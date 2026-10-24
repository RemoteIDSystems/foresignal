//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_block_one_of.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportBlockOneOf {
  /// Returns a new [ReportBlockOneOf] instance.
  ReportBlockOneOf({

     this.id,

    required  this.type,

     this.title,

     this.textOrigin,

     this.updatedAt,

     this.updatedBy,

    required  this.markdown,

     this.citations,
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


  final ReportBlockOneOfTypeEnum type;



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


  final ReportBlockOneOfTextOriginEnum? textOrigin;



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
    
    name: r'markdown',
    required: true,
    includeIfNull: false,
  )


  final String markdown;



      /// Verified citations (LLM narrative)
  @JsonKey(
    
    name: r'citations',
    required: false,
    includeIfNull: false,
  )


  final List<String>? citations;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportBlockOneOf &&
      other.id == id &&
      other.type == type &&
      other.title == title &&
      other.textOrigin == textOrigin &&
      other.updatedAt == updatedAt &&
      other.updatedBy == updatedBy &&
      other.markdown == markdown &&
      other.citations == citations;

    @override
    int get hashCode =>
        id.hashCode +
        type.hashCode +
        (title == null ? 0 : title.hashCode) +
        textOrigin.hashCode +
        updatedAt.hashCode +
        (updatedBy == null ? 0 : updatedBy.hashCode) +
        markdown.hashCode +
        citations.hashCode;

  factory ReportBlockOneOf.fromJson(Map<String, dynamic> json) => _$ReportBlockOneOfFromJson(json);

  Map<String, dynamic> toJson() => _$ReportBlockOneOfToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ReportBlockOneOfTypeEnum {
@JsonValue(r'markdown')
markdown(r'markdown');

const ReportBlockOneOfTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// `llm` blocks are labeled narrative from the optional 028 tool loop
enum ReportBlockOneOfTextOriginEnum {
@JsonValue(r'author')
author(r'author'),
@JsonValue(r'deterministic')
deterministic(r'deterministic'),
@JsonValue(r'llm')
llm(r'llm');

const ReportBlockOneOfTextOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


