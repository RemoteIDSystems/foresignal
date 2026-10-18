//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_item_rejection_schema_errors_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpItemRejectionSchemaErrorsInner {
  /// Returns a new [EdpItemRejectionSchemaErrorsInner] instance.
  EdpItemRejectionSchemaErrorsInner({

     this.path,

     this.message,
  });

  @JsonKey(
    
    name: r'path',
    required: false,
    includeIfNull: false,
  )


  final String? path;



  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpItemRejectionSchemaErrorsInner &&
      other.path == path &&
      other.message == message;

    @override
    int get hashCode =>
        path.hashCode +
        message.hashCode;

  factory EdpItemRejectionSchemaErrorsInner.fromJson(Map<String, dynamic> json) => _$EdpItemRejectionSchemaErrorsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$EdpItemRejectionSchemaErrorsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

