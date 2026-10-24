//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_block_base.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportBlockBase {
  /// Returns a new [ReportBlockBase] instance.
  ReportBlockBase({

     this.id,

    required  this.type,

     this.title,

     this.textOrigin,

     this.updatedAt,

     this.updatedBy,
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


  final ReportBlockBaseTypeEnum type;



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


  final ReportBlockBaseTextOriginEnum? textOrigin;



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





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportBlockBase &&
      other.id == id &&
      other.type == type &&
      other.title == title &&
      other.textOrigin == textOrigin &&
      other.updatedAt == updatedAt &&
      other.updatedBy == updatedBy;

    @override
    int get hashCode =>
        id.hashCode +
        type.hashCode +
        (title == null ? 0 : title.hashCode) +
        textOrigin.hashCode +
        updatedAt.hashCode +
        (updatedBy == null ? 0 : updatedBy.hashCode);

  factory ReportBlockBase.fromJson(Map<String, dynamic> json) => _$ReportBlockBaseFromJson(json);

  Map<String, dynamic> toJson() => _$ReportBlockBaseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ReportBlockBaseTypeEnum {
@JsonValue(r'markdown')
markdown(r'markdown'),
@JsonValue(r'bluf')
bluf(r'bluf'),
@JsonValue(r'objects')
objects(r'objects'),
@JsonValue(r'map')
map(r'map'),
@JsonValue(r'timeline')
timeline(r'timeline'),
@JsonValue(r'hypotheses')
hypotheses(r'hypotheses'),
@JsonValue(r'forecasts')
forecasts(r'forecasts'),
@JsonValue(r'evidence')
evidence(r'evidence'),
@JsonValue(r'coverage')
coverage(r'coverage'),
@JsonValue(r'attachments')
attachments(r'attachments'),
@JsonValue(r'sources')
sources(r'sources');

const ReportBlockBaseTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// `llm` blocks are labeled narrative from the optional 028 tool loop
enum ReportBlockBaseTextOriginEnum {
@JsonValue(r'author')
author(r'author'),
@JsonValue(r'deterministic')
deterministic(r'deterministic'),
@JsonValue(r'llm')
llm(r'llm');

const ReportBlockBaseTextOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


