package com.studentdata.control;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import com.studentdata.dto.Student;
import com.studentdata.service.StudentService;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/student")
public class StudentController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	StudentService service = new StudentService();
	Student student = new Student();
	RequestDispatcher dispatcher;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String dashboardUrl = request.getServletContext().getInitParameter("BASE_URL") + "/admin/dashboard";
		String stuId = request.getParameter("id");
		String action = request.getParameter("act");
		int id = 0;
		if (stuId != null) {
			id = Integer.parseInt(stuId);
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy");
		long now = System.currentTimeMillis();
		Date currdate = new Date(now);
		String firstname = request.getParameter("firstname");
		String lastname = request.getParameter("lastname");
		String birthday = request.getParameter("birthday");
		String gender = request.getParameter("gender");
		String state = request.getParameter("state");
		String district = request.getParameter("district");
		String contact = request.getParameter("contact");
		String username = request.getParameter("username");
		Date date = null;
		try {
			date = Date.valueOf(birthday);
			String curryear = df.format(currdate);
			String year = df.format(date);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		student.setFirstname(firstname);
		student.setLastname(lastname);
		student.setBirthday(date);
		student.setGender(gender);
		student.setState(state);
		student.setDistrict(district);
		if (id == 0) {
			if (service.usernameExist(username)) {
				request.getSession().setAttribute("userexist", "fail");
			} else {
				student.setUsername(username);
			}
		}
		if (service.contactExist(contact, id)) {
			request.getSession().setAttribute("contactexist", "fail");
		} else {
			student.setContact(contact);
		}
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<Student>> constraintViolations = validator.validate(student);
		if (!constraintViolations.isEmpty()) {
			String error;
			List<String> errors = new ArrayList<String>();
			for (ConstraintViolation<Student> constraintViolation : constraintViolations) {
				if (action != null && action.equals("edit")) {
					String propertyPath = constraintViolation.getPropertyPath().toString();
					if (propertyPath.equals("username")) {
						;
					} else {
						error = propertyPath + " " + constraintViolation.getMessage();
						errors.add(error);
					}
				} else if (action != null && action.equals("add")) {
					String propertyPath = constraintViolation.getPropertyPath().toString();
					error = propertyPath + " " + constraintViolation.getMessage();
					errors.add(error);
				}
			}
			request.setAttribute("student", student);
			request.setAttribute("errors", errors);
			if (id == 0)
				request.getRequestDispatcher("student_add.jsp").forward(request, response);
			else
				request.getRequestDispatcher("student_edit.jsp").forward(request, response);
		} else {
			int result = service.save(student, id);
			System.out.println(result + " " + id);
			if (result == 1 && id == 0) {
				request.getSession().setAttribute("add", "success");
			} else if (result != 1) {
				request.getSession().setAttribute("add", "fail");
			}
			if (result == 1 && id > 0) {
				request.getSession().setAttribute("edit", "success");
			} else if (result != 1) {
				request.getSession().setAttribute("edit", "fail");
			}
			response.sendRedirect(dashboardUrl);
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String dashboardUrl = request.getServletContext().getInitParameter("BASE_URL") + "/admin/dashboard";
		String action = request.getParameter("act");
		if (action != null) {
			switch (action) {
			case "add":
				dispatcher = request.getRequestDispatcher("student_add.jsp");
				dispatcher.forward(request, response);
				break;

			case "edit":
				String[] studentEditId = request.getParameterValues("id");
				if (studentEditId != null) {
					if (studentEditId.length == 1) {
						int studentid = Integer.parseInt(studentEditId[0]);
						student = service.getById(studentid);
						request.setAttribute("student", student);
						dispatcher = request.getRequestDispatcher("student_edit.jsp");
						dispatcher.forward(request, response);
					} else {
						request.getSession().setAttribute("edit", "checkmarks");
						response.sendRedirect(dashboardUrl);
					}
				} else {
					response.sendRedirect(dashboardUrl);
				}
				break;

			case "delete":
				String[] studentDeleteIds = request.getParameterValues("id");
				if (studentDeleteIds != null) {
					int result = service.delete(studentDeleteIds);
					if (result > 0) {
						request.setAttribute("delete", "success");
						dispatcher = request.getRequestDispatcher("dashboard");
						dispatcher.forward(request, response);
					} else {
						request.setAttribute("delete", "fail");
						dispatcher = request.getRequestDispatcher("dashboard");
						dispatcher.forward(request, response);
					}
				}
				response.sendRedirect(dashboardUrl);
				break;

			default:
				break;
			}
		}
	}
}