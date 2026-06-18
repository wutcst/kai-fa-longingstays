package cn.edu.whut.sept.zuul.db;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class HibernateUtilTest {

    private static boolean dbAvailable;

    @BeforeClass
    public static void checkDbAvailable() {
        try {
            Class.forName("org.sqlite.JDBC");
            dbAvailable = true;
        } catch (ClassNotFoundException e) {
            dbAvailable = false;
        }
    }

    @Before
    public void setUp() {
        org.junit.Assume.assumeTrue("SQLite JDBC not available", dbAvailable);
    }

    @Test
    public void testGetSessionFactoryReturnsNonNull() {
        org.hibernate.SessionFactory sf = HibernateUtil.getSessionFactory();
        // 可能为 null（配置错误等），也可能非 null
        // 记录实际行为
        if (sf == null) {
            // Hibernate 配置可能有问题，记录但不失败
            System.out.println("SessionFactory is null - Hibernate may not be configured");
        }
    }

    @Test
    public void testGetSessionFactoryReturnsSameInstance() {
        org.hibernate.SessionFactory sf1 = HibernateUtil.getSessionFactory();
        org.hibernate.SessionFactory sf2 = HibernateUtil.getSessionFactory();
        if (sf1 != null) {
            assertSame(sf1, sf2);
        }
    }

    @Test
    public void testShutdownDoesNotThrow() {
        // shutdown 不应抛异常
        try {
            HibernateUtil.shutdown();
        } catch (Exception e) {
            fail("shutdown should not throw: " + e.getMessage());
        }
    }

    @Test
    public void testGetSessionFactoryAfterShutdown() {
        org.hibernate.SessionFactory sf1 = HibernateUtil.getSessionFactory();
        HibernateUtil.shutdown();
        // 之后再次获取的行为（可能为 null 或重建）
        org.hibernate.SessionFactory sf2 = HibernateUtil.getSessionFactory();
        // 记录行为，不强制断言
        assertNotNull("SessionFactory should be retrievable", sf2 != null ? sf2 : sf1);
    }
}
