//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_block_one_of9.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportBlockOneOf9 {
  /// Returns a new [ReportBlockOneOf9] instance.
  ReportBlockOneOf9({

     this.id,

    required  this.type,

     this.title,

     this.textOrigin,

     this.updatedAt,

     this.updatedBy,

    required  this.attachmentIds,
  });

      /// Server-assigned; comment on a block with `report:{id}#{blockId}`
  @JsonKey(
    
    name: r'id',
    required: false,
    includeIfNull: false,
  )


  final String? id;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ReportBlockOneOf9TypeEnum type;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



      /// `llm` blocks are labeled narrative from the optional 028 tool loop
  @JsonKey(
    
    name: r'textOrigin',
    required: false,
    includeIfNull: false,
  )


  final ReportBlockOneOf9TextOriginEnum? textOrigin;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;



  @JsonKey(
    
    name: r'updatedBy',
    required: false,
    includeIfNull: false,
  )


  final PrincipalSummary? updatedBy;



  @JsonKey(
    
    name: r'attachmentIds',
    required: true,
    includeIfNull: false,
  )


  final List<String> attachmentIds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportBlockOneOf9 &&
      other.id == id &&
      other.type == type &&
      other.title == title &&
      other.textOrigin == textOrigin &&
      other.updatedAt == updatedAt &&
      other.updatedBy == updatedBy &&
      other.attachmentIds == attachmentIds;

    @override
    int get hashCode =>
        id.hashCode +
        type.hashCode +
        (title == null ? 0 : title.hashCode) +
        textOrigin.hashCode +
        updatedAt.hashCode +
        (updatedBy == null ? 0 : updatedBy.hashCode) +
        attachmentIds.hashCode;

  factory ReportBlockOneOf9.fromJson(Map<String, dynamic> json) => _$ReportBlockOneOf9FromJson(json);

  Map<String, dynamic> toJson() => _$ReportBlockOneOf9ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ReportBlockOneOf9TypeEnum {
@JsonValue(r'attachments')
attachments(r'attachments');

const ReportBlockOneOf9TypeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// `llm` blocks are labeled narrative from the optional 028 tool loop
enum ReportBlockOneOf9TextOriginEnum {
@JsonValue(r'author')
author(r'author'),
@JsonValue(r'deterministic')
deterministic(r'deterministic'),
@JsonValue(r'llm')
llm(r'llm');

const ReportBlockOneOf9TextOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


