<%@page contentType="text/html;charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<body>

<table border="1">
    <thead>
    <tr>
        <th>First name</th>
        <th>Surname</th>
        <th>Primary email</th>
        <th>Birthday</th>
    </tr>
    </thead>
    <c:forEach items="${contacts}" var="contact">
        <tr>
            <td><c:out value="${contact.firstName}"/></td>
            <td><c:out value="${contact.surname}"/></td>
            <td><c:out value="${contact.primaryEmail}"/></td>
            <td><c:out value="${contact.birthday}"/></td>
            <td><form method="post" action="${pageContext.request.contextPath}/books/delete?id=${contact.id}"
                      style="margin-bottom: 0;"><input type="submit" value="Smazat"></form></td>
        </tr>
    </c:forEach>
</table>

<h2>Insert contact</h2>
<c:if test="${not empty validation_error}">
    <div style="border: solid 1px red; background-color: yellow; padding: 10px">
        <c:out value="${validation_error}"/>
    </div>
</c:if>
<form action="${pageContext.request.contextPath}/contacts/add" method="post">
    <table>
        <tr>
            <th>First name:</th>
            <td><input type="text" name="firstName" value="<c:out value='${param.firstName}'/>"/></td>
        </tr>
        <tr>
            <th>Surname:</th>
            <td><input type="text" name="surname" value="<c:out value='${param.surname}'/>"/></td>
        </tr>
        <tr>
            <th>Primary email:</th>
            <td><input type="text" name="primaryEmail" value="<c:out value='${param.primaryEmail}'/>"/></td>
        </tr>
        <tr>
            <th>Birthday:</th>
            <td><input type="text" name="birthday" value="<c:out value='${param.birthday}'/>"/></td>
        </tr>
    </table>
    <input type="Submit" value="enter" />
</form>

</body>
</html>