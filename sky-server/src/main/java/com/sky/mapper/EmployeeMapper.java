package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface EmployeeMapper {

  /**
   * 根据用户名查询员工
   *
   * @param username
   * @return
   */
  @Select("select * from employee   where username = #{username}")
  Employee getByUsername(String username);

  /**
   * 新增员工
   *
   * @param employee
   */
  @Insert("insert into employee (name, username, password, phone, sex, id_number, create_time, update_time, create_user, update_user, status) values (#{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser}, #{status});")
  void insert(Employee employee);


  /**
   * 员工分页查询
   * @param employeePageQueryDTO
   * @return
   */
  Page<Employee> page(EmployeePageQueryDTO employeePageQueryDTO);


  /**
   * 更新员工信息
   * @param employee
   */
  void update(Employee employee);

  /**
   * 根据id查询员工信息
   * @param id
   * @return
   */
  @Select("select * from employee where id = #{id}")
  Employee employeeById(Long id);

//  @Select("select count(*) from employee")
//  Long total();
//
//  @Select("select * from employee limit #{offset},#{limit};")
//  List<Employee> pageList(int offset, int limit);

}
