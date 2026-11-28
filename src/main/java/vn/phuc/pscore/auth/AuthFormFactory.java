package vn.phuc.pscore.auth;

import java.util.List;

public final class AuthFormFactory {

    private AuthFormFactory() {
    }

    public static AuthFormDefinition register() {
        return new AuthFormDefinition(
                AuthFormType.REGISTER,
                "Đăng ký tài khoản",
                "Tạo mật khẩu để bảo vệ tài khoản của bạn.",
                List.of(
                        new AuthFormField(
                                "password",
                                "Mật khẩu",
                                "Nhập mật khẩu",
                                true,
                                true,
                                64
                        ),
                        new AuthFormField(
                                "confirm",
                                "Xác nhận mật khẩu",
                                "Nhập lại mật khẩu",
                                true,
                                true,
                                64
                        )
                ),
                "Đăng ký",
                "Thoát"
        );
    }

    public static AuthFormDefinition login() {
        return new AuthFormDefinition(
                AuthFormType.LOGIN,
                "Đăng nhập",
                "Nhập mật khẩu tài khoản của bạn.",
                List.of(
                        new AuthFormField(
                                "password",
                                "Mật khẩu",
                                "Nhập mật khẩu",
                                true,
                                true,
                                64
                        )
                ),
                "Đăng nhập",
                "Thoát"
        );
    }

    public static AuthFormDefinition changePassword() {
        return new AuthFormDefinition(
                AuthFormType.CHANGE_PASSWORD,
                "Đổi mật khẩu",
                "Nhập mật khẩu hiện tại và mật khẩu mới.",
                List.of(
                        new AuthFormField(
                                "current",
                                "Mật khẩu hiện tại",
                                "Nhập mật khẩu hiện tại",
                                true,
                                true,
                                64
                        ),
                        new AuthFormField(
                                "password",
                                "Mật khẩu mới",
                                "Nhập mật khẩu mới",
                                true,
                                true,
                                64
                        ),
                        new AuthFormField(
                                "confirm",
                                "Xác nhận mật khẩu mới",
                                "Nhập lại mật khẩu mới",
                                true,
                                true,
                                64
                        )
                ),
                "Đổi mật khẩu",
                "Hủy"
        );
    }
}
