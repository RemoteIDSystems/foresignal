//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_block_one_of3.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportBlockOneOf3 {
  /// Returns a new [ReportBlockOneOf3] instance.
  ReportBlockOneOf3({

     this.id,

    required  this.type,

     this.title,

     this.textOrigin,

     this.updatedAt,

     this.updatedBy,

     this.layers,

     this.area,

     this.bbox,

     this.timeRange,

     this.attachmentId,

     this.mediaId,

     this.caveat,
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


  final ReportBlockOneOf3TypeEnum type;



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


  final ReportBlockOneOf3TextOriginEnum? textOrigin;



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
    
    name: r'layers',
    required: false,
    includeIfNull: false,
  )


  final List<String>? layers;



  @JsonKey(
    
    name: r'area',
    required: false,
    includeIfNull: false,
  )


  final Geometry? area;



  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bbox;



  @JsonKey(
    
    name: r'timeRange',
    required: false,
    includeIfNull: false,
  )


  final TimeRange? timeRange;



      /// ULID identifier
  @JsonKey(
    
    name: r'attachmentId',
    required: false,
    includeIfNull: false,
  )


  final String? attachmentId;



  @JsonKey(
    
    name: r'mediaId',
    required: false,
    includeIfNull: false,
  )


  final String? mediaId;



      /// Why the map was not rendered at publication, if it was not
  @JsonKey(
    
    name: r'caveat',
    required: false,
    includeIfNull: false,
  )


  final String? caveat;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportBlockOneOf3 &&
      other.id == id &&
      other.type == type &&
      other.title == title &&
      other.textOrigin == textOrigin &&
      other.updatedAt == updatedAt &&
      other.updatedBy == updatedBy &&
      other.layers == layers &&
      other.area == area &&
      other.bbox == bbox &&
      other.timeRange == timeRange &&
      other.attachmentId == attachmentId &&
      other.mediaId == mediaId &&
      other.caveat == caveat;

    @override
    int get hashCode =>
        id.hashCode +
        type.hashCode +
        (title == null ? 0 : title.hashCode) +
        textOrigin.hashCode +
        updatedAt.hashCode +
        (updatedBy == null ? 0 : updatedBy.hashCode) +
        layers.hashCode +
        (area == null ? 0 : area.hashCode) +
        bbox.hashCode +
        (timeRange == null ? 0 : timeRange.hashCode) +
        (attachmentId == null ? 0 : attachmentId.hashCode) +
        (mediaId == null ? 0 : mediaId.hashCode) +
        (caveat == null ? 0 : caveat.hashCode);

  factory ReportBlockOneOf3.fromJson(Map<String, dynamic> json) => _$ReportBlockOneOf3FromJson(json);

  Map<String, dynamic> toJson() => _$ReportBlockOneOf3ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ReportBlockOneOf3TypeEnum {
@JsonValue(r'map')
map(r'map');

const ReportBlockOneOf3TypeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// `llm` blocks are labeled narrative from the optional 028 tool loop
enum ReportBlockOneOf3TextOriginEnum {
@JsonValue(r'author')
author(r'author'),
@JsonValue(r'deterministic')
deterministic(r'deterministic'),
@JsonValue(r'llm')
llm(r'llm');

const ReportBlockOneOf3TextOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


