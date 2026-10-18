//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/edp_item_rejection_schema_errors_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_item_rejection.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpItemRejection {
  /// Returns a new [EdpItemRejection] instance.
  EdpItemRejection({

    required  this.index,

     this.stream,

     this.id,

    required  this.reason,

     this.schemaErrors,
  });

  @JsonKey(
    
    name: r'index',
    required: true,
    includeIfNull: false,
  )


  final int index;



  @JsonKey(
    
    name: r'stream',
    required: false,
    includeIfNull: false,
  )


  final String? stream;



  @JsonKey(
    
    name: r'id',
    required: false,
    includeIfNull: false,
  )


  final String? id;



  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final String reason;



  @JsonKey(
    
    name: r'schemaErrors',
    required: false,
    includeIfNull: false,
  )


  final List<EdpItemRejectionSchemaErrorsInner>? schemaErrors;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpItemRejection &&
      other.index == index &&
      other.stream == stream &&
      other.id == id &&
      other.reason == reason &&
      other.schemaErrors == schemaErrors;

    @override
    int get hashCode =>
        index.hashCode +
        (stream == null ? 0 : stream.hashCode) +
        (id == null ? 0 : id.hashCode) +
        reason.hashCode +
        schemaErrors.hashCode;

  factory EdpItemRejection.fromJson(Map<String, dynamic> json) => _$EdpItemRejectionFromJson(json);

  Map<String, dynamic> toJson() => _$EdpItemRejectionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

