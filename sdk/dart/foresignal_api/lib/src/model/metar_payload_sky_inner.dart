//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'metar_payload_sky_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MetarPayloadSkyInner {
  /// Returns a new [MetarPayloadSkyInner] instance.
  MetarPayloadSkyInner({

    required  this.cover,

     this.baseFtAgl,
  });

  @JsonKey(
    
    name: r'cover',
    required: true,
    includeIfNull: false,
  )


  final String cover;



  @JsonKey(
    
    name: r'baseFtAgl',
    required: false,
    includeIfNull: false,
  )


  final int? baseFtAgl;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MetarPayloadSkyInner &&
      other.cover == cover &&
      other.baseFtAgl == baseFtAgl;

    @override
    int get hashCode =>
        cover.hashCode +
        baseFtAgl.hashCode;

  factory MetarPayloadSkyInner.fromJson(Map<String, dynamic> json) => _$MetarPayloadSkyInnerFromJson(json);

  Map<String, dynamic> toJson() => _$MetarPayloadSkyInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

