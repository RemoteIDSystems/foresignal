//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/report_block.dart';
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:foresignal_api/src/model/visibility.dart';
import 'package:foresignal_api/src/model/report_source.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportInput {
  /// Returns a new [ReportInput] instance.
  ReportInput({

    required  this.title,

     this.subtitle,

     this.authors,

     this.tlp,

     this.visibility,

     this.source_,

     this.template,

     this.timeWindow,

     this.area,

     this.blocks,
  });

  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'subtitle',
    required: false,
    includeIfNull: false,
  )


  final String? subtitle;



      /// Principal ids or handles; the caller is always an author
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
    
    name: r'source',
    required: false,
    includeIfNull: false,
  )


  final ReportSource? source_;



  @JsonKey(
    
    name: r'template',
    required: false,
    includeIfNull: false,
  )


  final ReportInputTemplateEnum? template;



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



  @JsonKey(
    
    name: r'blocks',
    required: false,
    includeIfNull: false,
  )


  final List<ReportBlock>? blocks;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportInput &&
      other.title == title &&
      other.subtitle == subtitle &&
      other.authors == authors &&
      other.tlp == tlp &&
      other.visibility == visibility &&
      other.source_ == source_ &&
      other.template == template &&
      other.timeWindow == timeWindow &&
      other.area == area &&
      other.blocks == blocks;

    @override
    int get hashCode =>
        title.hashCode +
        (subtitle == null ? 0 : subtitle.hashCode) +
        authors.hashCode +
        tlp.hashCode +
        visibility.hashCode +
        source_.hashCode +
        template.hashCode +
        (timeWindow == null ? 0 : timeWindow.hashCode) +
        (area == null ? 0 : area.hashCode) +
        blocks.hashCode;

  factory ReportInput.fromJson(Map<String, dynamic> json) => _$ReportInputFromJson(json);

  Map<String, dynamic> toJson() => _$ReportInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ReportInputTemplateEnum {
@JsonValue(r'situation-brief')
situationBrief(r'situation-brief'),
@JsonValue(r'investigation-summary')
investigationSummary(r'investigation-summary'),
@JsonValue(r'chronicle-recap')
chronicleRecap(r'chronicle-recap'),
@JsonValue(r'weekly-assessment')
weeklyAssessment(r'weekly-assessment'),
@JsonValue(r'custom')
custom(r'custom');

const ReportInputTemplateEnum(this.value);

final String value;

@override
String toString() => value;
}


