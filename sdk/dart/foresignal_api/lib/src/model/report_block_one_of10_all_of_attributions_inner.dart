//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_block_one_of10_all_of_attributions_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportBlockOneOf10AllOfAttributionsInner {
  /// Returns a new [ReportBlockOneOf10AllOfAttributionsInner] instance.
  ReportBlockOneOf10AllOfAttributionsInner({

    required  this.sourceId,

     this.licence,

    required  this.attribution,

     this.url,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'licence',
    required: false,
    includeIfNull: false,
  )


  final String? licence;



  @JsonKey(
    
    name: r'attribution',
    required: true,
    includeIfNull: false,
  )


  final String attribution;



  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportBlockOneOf10AllOfAttributionsInner &&
      other.sourceId == sourceId &&
      other.licence == licence &&
      other.attribution == attribution &&
      other.url == url;

    @override
    int get hashCode =>
        sourceId.hashCode +
        (licence == null ? 0 : licence.hashCode) +
        attribution.hashCode +
        (url == null ? 0 : url.hashCode);

  factory ReportBlockOneOf10AllOfAttributionsInner.fromJson(Map<String, dynamic> json) => _$ReportBlockOneOf10AllOfAttributionsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ReportBlockOneOf10AllOfAttributionsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

