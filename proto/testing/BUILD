proto_library(
    name = "test_proto",
    testonly = 1,
    srcs = ["test.proto"],
)

java_proto_library(
    name = "test_java_proto",
    testonly = 1,
    visibility = ["//visibility:public"],
    deps = [":test_proto"],
)
