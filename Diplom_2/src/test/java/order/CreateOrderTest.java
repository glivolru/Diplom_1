package order;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pens.IngredientApiRequests;
import pens.OrderApiRequests;
import pens.UserApiRequests;
import serialization.Ingredient;
import serialization.Order;
import serialization.User;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;

public class CreateOrderTest {

    UserApiRequests userApiRequests = new UserApiRequests();
    OrderApiRequests orderApiRequests = new OrderApiRequests();
    IngredientApiRequests ingredientApiRequests = new IngredientApiRequests();
    User user = new User("ivanglushenkov@yandex.ru", "Ivan", "qwerty12345");

    private String accessToken;
    private Ingredient ingredientList;
    private List<String> ingredients;
    private Order order;

    @Before
    public void setUp() {
        url.BaseUrl.setUp();
        ingredientList = ingredientApiRequests.getIngredientInfo();
        ingredients = new ArrayList<>();
        ingredients.add(ingredientList.getData().get(1).get_id());
        ingredients.add(ingredientList.getData().get(2).get_id());
        ingredients.add(ingredientList.getData().get(3).get_id());
        userApiRequests.createUser(user);
        accessToken = userApiRequests.loginUser(user).then().extract().path("accessToken");
        order = new Order(ingredients);
    }

    @Test
    @DisplayName("Создание заказа авторизованным пользователем")
    public void testCreateOrderAuthUser() {
        Response response = orderApiRequests.createOrderAuthUser(order, accessToken);
        response.then().statusCode(200)
                .and()
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание заказа неавторизованным пользователем")
    public void testCreateOrderNotAuthUser() {
        Response response = orderApiRequests.createOrderNotAuthUser(order);
        response.then().statusCode(200)
                .and()
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов, авторизованный пользователь")
    public void testCreateOrderNoIngredients() {
        ingredients.clear();
        order = new Order(ingredients);
        Response response = orderApiRequests.createOrderAuthUser(order, accessToken);
        response.then().statusCode(400)
                .and()
                .body("success", equalTo(false));
    }

    @Test
    @DisplayName("Создание заказа c неверным хешем ингредиентов")
    public void testCreateOrderWithWrongHash() {
        ingredients.clear();
        ingredients.add("wrong");
        order = new Order(ingredients);
        Response response = orderApiRequests.createOrderAuthUser(order, accessToken);
        response.then().statusCode(500);
    }

    @After
    public void deleteUser() {
        if (accessToken != null) {
            userApiRequests.deleteUser(accessToken);
        }
    }
}
