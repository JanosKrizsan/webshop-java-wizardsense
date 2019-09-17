package com.codecool.shop.controller;

import com.codecool.shop.dao.GenericQueriesDao;
import com.codecool.shop.dao.ProductDao;
import com.codecool.shop.dao.implementation.Memory.CartDaoMem;
import com.codecool.shop.dao.implementation.Memory.ProductCategoryDaoMem;
import com.codecool.shop.dao.implementation.Memory.ProductDaoMem;
import com.codecool.shop.config.TemplateEngineUtil;
import com.codecool.shop.dao.implementation.Memory.SupplierDaoMem;
import com.codecool.shop.model.Cart;
import com.codecool.shop.model.ProductCategory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@WebServlet(urlPatterns = {"/"})
public class ProductController extends HttpServlet {

    private Object defaultProds = null;

    private void filter(GenericQueriesDao<ProductCategory> pCD, ProductDao pDS, HttpServletRequest req) {
        List<String> headers = Collections.list(req.getParameterNames());

        if (headers.contains("filter")) {
            List<Character> filtered = req.getParameter("filter").chars().mapToObj(e -> (char) e).collect(Collectors.toList());
            int filterId = Integer.parseInt(filtered.get(filtered.size() - 1).toString());
            filtered.remove(filtered.size() - 1);

            StringBuilder sBuilder = new StringBuilder();
            for (Character character : filtered) {
                sBuilder.append(character);
            }

            String filterName = sBuilder.toString().trim();
            String suppliers = SupplierDaoMem.getInstance().getAll().toString();

            if (suppliers.contains(filterName)) {
                defaultProds = pDS.getBy(SupplierDaoMem.getInstance().find(filterId));
            } else {
                defaultProds = pDS.getBy(pCD.find(filterId));
            }
        } else if (headers.contains("reset")) {
            defaultProds = pDS.getAll();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ProductDao productDataStore = ProductDaoMem.getInstance();
        GenericQueriesDao<ProductCategory> productCategoryDataStore = ProductCategoryDaoMem.getInstance();

        int cartSize = CartDaoMem.getInstance().find(0).getSumOfProducts();

        TemplateEngine engine = TemplateEngineUtil.getTemplateEngine(req.getServletContext());
        WebContext context = new WebContext(req, resp, req.getServletContext());


        filter(productCategoryDataStore, productDataStore, req);
        context.setVariable("categories", productCategoryDataStore.getAll());
        context.setVariable("suppliers", SupplierDaoMem.getInstance().getAll());
        context.setVariable("cartSize", cartSize);
        context.setVariable("products", defaultProds != null ? defaultProds : productDataStore.getAll());


        engine.process("product/index.html", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            ProductDao productDataStore = ProductDaoMem.getInstance();
            Cart cartData = CartDaoMem.getInstance().find(0);

            int productId = Integer.parseInt(req.getParameter("product"));
            cartData.addProduct(productDataStore.find(productId));
        } catch (NumberFormatException e) {
            System.out.println(e);
        }


        doGet(req, resp);
    }

}

// // Alternative setting of the template context
// Map<String, Object> params = new HashMap<>();
// params.put("category", productCategoryDataStore.find(1));
// params.put("products", productDataStore.getBy(productCategoryDataStore.find(1)));
// context.setVariables(params);