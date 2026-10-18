//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:foresignal_api/src/model/visibility.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportPatch {
  /// Returns a new [ReportPatch] instance.
  ReportPatch({

     this.title,

     this.subtitle,

     this.status,

     this.authors,

     this.tlp,

     this.visibility,

     this.timeWindow,

     this.area,

     this.blockOrder,

     this.version,
  });

  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'subtitle',
    required: false,
    includeIfNull: false,
  )


  final String? subtitle;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final ReportPatchStatusEnum? status;



  @JsonKey(
    
    name: r'authors',
    required: false,
    includeIfNull: false,
  )


  final List<String>? authors;



  @JsonKey(
    
    name: r'tlp',
    required: false,
    includeIfNull: false,
  )


  final Tlp? tlp;



  @JsonKey(
    
    name: r'visibility',
    required: false,
    includeIfNull: false,
  )


  final Visibility? visibility;



  @JsonKey(
    
    name: r'timeWindow',
    required: false,
    includeIfNull: false,
  )


  final TimeRange? timeWindow;



  @JsonKey(
    
    name: r'area',
    required: false,
    includeIfNull: false,
  )


  final Geometry? area;



      /// Reorder blocks by id
  @JsonKey(
    
    name: r'blockOrder',
    required: false,
    includeIfNull: false,
  )


  final List<String>? blockOrder;



  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final int? version;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportPatch &&
      other.title == title &&
      other.subtitle == subtitle &&
      other.status == status &&
      other.authors == authors &&
      other.tlp == tlp &&
      other.visibility == visibility &&
      other.timeWindow == timeWindow &&
      other.area == area &&
      other.blockOrder == blockOrder &&
      other.version == version;

    @override
    int get hashCode =>
        title.hashCode +
        (subtitle == null ? 0 : subtitle.hashCode) +
        status.hashCode +
        authors.hashCode +
        tlp.hashCode +
        visibility.hashCode +
        (timeWindow == null ? 0 : timeWindow.hashCode) +
        (area == null ? 0 : area.hashCode) +
        blockOrder.hashCode +
        version.hashCode;

  factory ReportPatch.fromJson(Map<String, dynamic> json) => _$ReportPatchFromJson(json);

  Map<String, dynamic> toJson() => _$ReportPatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ReportPatchStatusEnum {
@JsonValue(r'draft')
draft(r'draft'),
@JsonValue(r'review')
review(r'review');

const ReportPatchStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


