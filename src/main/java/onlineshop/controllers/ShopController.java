package onlineshop.controllers;

import jakarta.servlet.http.HttpSession;
import onlineshop.Cart;
import onlineshop.Shop;
import onlineshop.enums.Sorting;
import onlineshop.merchandise.Book;
import onlineshop.merchandise.CartItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class ShopController {
    private final static Logger log = LogManager.getLogger(ShopController.class);
    public static final int PAGE_SIZE = 15;

    @Autowired
    Shop shop;

    @Autowired
    Cart cart;

    @GetMapping(value = {"/"})
    public String root() {
        return "redirect:/index.html";
    }

    @GetMapping(value = {"/index.html"})
    public String homePage(Model view,
                           @RequestParam(name = "page", required = false) Integer page,
                           @RequestParam(name = "sort", required = false) Sorting sort,
                           HttpSession session) {
        sort = (Sorting) getSessionParam(session, "sort", sort, Sorting.ALPHA_UP);
        page = (Integer) getSessionParam(session, "page", page, 1);
        handlePagination(view, sort, page);
        getCartItems(view);
        return "index";
    }

    @GetMapping(value = {"/{name}.html"})
    public String htmlMapping(Model view, @PathVariable(name = "name") String name) {
        getCartItems(view);
        return name;
    }

    /**
     * Loads the cart items from the cart object and stores the corresponding attributes in the view view.
     *
     * @param view {@link Model}
     */
    private void getCartItems(Model view) {
        List<CartItem> cartItems = cart.getItems();
        view.addAttribute("cartItems", cartItems);
        view.addAttribute("numOfCartItems", cart.getNumOfItems());
        view.addAttribute("grandTotal", cart.getGrandTotal());
    }

    /**
     * Looks up the requested parameter in  the session. If it doesn't exist, it uses the default value.
     *
     * @param session      {@link jakarta.servlet.http.HttpSession}
     * @param paramName    {@link String}
     * @param paramValue   {@link Object}
     * @param defaultValue {@link Object}
     * @return sessionValue {@link Object}
     */
    private Object getSessionParam(HttpSession session,
                                   String paramName,
                                   Object paramValue,
                                   Object defaultValue) {
        if (paramValue == null) {
            Object sessionValue = session.getAttribute(paramName);
            paramValue = sessionValue == null ? defaultValue : sessionValue;
        }
        session.setAttribute(paramName, paramValue);
        return paramValue;
    }

    /**
     * Delivers the articles sublist corresponding to the selected page
     *
     * @param view   {@link Model}
     * @param sorting {@link Sorting}
     * @param page    {@link Integer}
     */
    private void handlePagination(Model view, Sorting sorting, Integer page) {
        int numOfArticles = shop.getNumOfArticles();
        int from = Math.max((page - 1) * PAGE_SIZE, 0);
        int to = Math.min(numOfArticles, from + PAGE_SIZE);
        List<Book> articles = shop.getArticles(sorting, from, to);

        view.addAttribute("articles", articles);
        view.addAttribute("from", ++from);
        view.addAttribute("to", to);
        view.addAttribute("numOfArticles", numOfArticles);

        int pageCount = (numOfArticles / PAGE_SIZE) + 1;
        Map<Integer, String> pages = new HashMap<>();
        for (int pageNumber = 1; pageNumber <= pageCount; pageNumber++) {
            String active = (pageNumber == page) ? "active" : "";
            pages.put(pageNumber, active);
        }

        view.addAttribute("pageCount", pageCount);
        view.addAttribute("pages", pages.entrySet());
        view.addAttribute("prevPage", Math.max(page - 1, 1));
        view.addAttribute("nextPage", Math.min(page + 1, pageCount));

        handleSorting(view, sorting);
    }

    private void handleSorting(Model view, Sorting currentSort) {
        List<Sorting> sortings = new ArrayList<>();
        for (Sorting entry : Sorting.values()) {
            String isCurrentSort = (entry == currentSort) ? "selected" : "";
            entry.setSelected(isCurrentSort);
            sortings.add(entry);
        }
        view.addAttribute("sortings", sortings);
        view.addAttribute("sort", currentSort.getValue());
    }

}
