//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_revision_renditions.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportRevisionRenditions {
  /// Returns a new [ReportRevisionRenditions] instance.
  ReportRevisionRenditions({

     this.markdown,

     this.html,

     this.json,
  });

  @JsonKey(
    
    name: r'markdown',
    required: false,
    includeIfNull: false,
  )


  final String? markdown;



  @JsonKey(
    
    name: r'html',
    required: false,
    includeIfNull: false,
  )


  final String? html;



  @JsonKey(
    
    name: r'json',
    required: false,
    includeIfNull: false,
  )


  final String? json;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportRevisionRenditions &&
      other.markdown == markdown &&
      other.html == html &&
      other.json == json;

    @override
    int get hashCode =>
        markdown.hashCode +
        html.hashCode +
        json.hashCode;

  factory ReportRevisionRenditions.fromJson(Map<String, dynamic> json) => _$ReportRevisionRenditionsFromJson(json);

  Map<String, dynamic> toJson() => _$ReportRevisionRenditionsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

