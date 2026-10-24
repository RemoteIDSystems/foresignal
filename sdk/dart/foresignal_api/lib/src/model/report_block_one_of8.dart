//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/coverage_stats.dart';
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_block_one_of8.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportBlockOneOf8 {
  /// Returns a new [ReportBlockOneOf8] instance.
  ReportBlockOneOf8({

     this.id,

    required  this.type,

     this.title,

     this.textOrigin,

     this.updatedAt,

     this.updatedBy,

     this.caveats,

     this.coverage,
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


  final ReportBlockOneOf8TypeEnum type;



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


  final ReportBlockOneOf8TextOriginEnum? textOrigin;



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
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? caveats;



  @JsonKey(
    
    name: r'coverage',
    required: false,
    includeIfNull: false,
  )


  final List<CoverageStats>? coverage;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportBlockOneOf8 &&
      other.id == id &&
      other.type == type &&
      other.title == title &&
      other.textOrigin == textOrigin &&
      other.updatedAt == updatedAt &&
      other.updatedBy == updatedBy &&
      other.caveats == caveats &&
      other.coverage == coverage;

    @override
    int get hashCode =>
        id.hashCode +
        type.hashCode +
        (title == null ? 0 : title.hashCode) +
        textOrigin.hashCode +
        updatedAt.hashCode +
        (updatedBy == null ? 0 : updatedBy.hashCode) +
        caveats.hashCode +
        coverage.hashCode;

  factory ReportBlockOneOf8.fromJson(Map<String, dynamic> json) => _$ReportBlockOneOf8FromJson(json);

  Map<String, dynamic> toJson() => _$ReportBlockOneOf8ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ReportBlockOneOf8TypeEnum {
@JsonValue(r'coverage')
coverage(r'coverage');

const ReportBlockOneOf8TypeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// `llm` blocks are labeled narrative from the optional 028 tool loop
enum ReportBlockOneOf8TextOriginEnum {
@JsonValue(r'author')
author(r'author'),
@JsonValue(r'deterministic')
deterministic(r'deterministic'),
@JsonValue(r'llm')
llm(r'llm');

const ReportBlockOneOf8TextOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


