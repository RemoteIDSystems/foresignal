//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'glance_briefing.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class GlanceBriefing {
  /// Returns a new [GlanceBriefing] instance.
  GlanceBriefing({

     this.title,

     this.at,

     this.url,
  });

  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'at',
    required: false,
    includeIfNull: false,
  )


  final DateTime? at;



  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;





    @override
    bool operator ==(Object other) => identical(this, other) || other is GlanceBriefing &&
      other.title == title &&
      other.at == at &&
      other.url == url;

    @override
    int get hashCode =>
        title.hashCode +
        at.hashCode +
        url.hashCode;

  factory GlanceBriefing.fromJson(Map<String, dynamic> json) => _$GlanceBriefingFromJson(json);

  Map<String, dynamic> toJson() => _$GlanceBriefingToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

