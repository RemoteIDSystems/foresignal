//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/source_descriptor.dart';
import 'package:foresignal_api/src/model/source_health.dart';
import 'package:foresignal_api/src/model/source_settings.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'source_detail.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SourceDetail {
  /// Returns a new [SourceDetail] instance.
  SourceDetail({

    required  this.descriptor,

    required  this.settings,

    required  this.health,
  });

  @JsonKey(
    
    name: r'descriptor',
    required: true,
    includeIfNull: false,
  )


  final SourceDescriptor descriptor;



  @JsonKey(
    
    name: r'settings',
    required: true,
    includeIfNull: false,
  )


  final SourceSettings settings;



  @JsonKey(
    
    name: r'health',
    required: true,
    includeIfNull: false,
  )


  final SourceHealth health;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SourceDetail &&
      other.descriptor == descriptor &&
      other.settings == settings &&
      other.health == health;

    @override
    int get hashCode =>
        descriptor.hashCode +
        settings.hashCode +
        health.hashCode;

  factory SourceDetail.fromJson(Map<String, dynamic> json) => _$SourceDetailFromJson(json);

  Map<String, dynamic> toJson() => _$SourceDetailToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

