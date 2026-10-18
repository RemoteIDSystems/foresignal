//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'storm_swath_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StormSwathPayload {
  /// Returns a new [StormSwathPayload] instance.
  StormSwathPayload({

    required  this.trackId,

     this.meshMaxMm,

    required  this.durationMin,

     this.status,
  });

  @JsonKey(
    
    name: r'trackId',
    required: true,
    includeIfNull: false,
  )


  final String trackId;



  @JsonKey(
    
    name: r'meshMaxMm',
    required: false,
    includeIfNull: false,
  )


  final num? meshMaxMm;



  @JsonKey(
    
    name: r'durationMin',
    required: true,
    includeIfNull: false,
  )


  final num durationMin;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final StormSwathPayloadStatusEnum? status;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StormSwathPayload &&
      other.trackId == trackId &&
      other.meshMaxMm == meshMaxMm &&
      other.durationMin == durationMin &&
      other.status == status;

    @override
    int get hashCode =>
        trackId.hashCode +
        (meshMaxMm == null ? 0 : meshMaxMm.hashCode) +
        durationMin.hashCode +
        status.hashCode;

  factory StormSwathPayload.fromJson(Map<String, dynamic> json) => _$StormSwathPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$StormSwathPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StormSwathPayloadStatusEnum {
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'ended')
ended(r'ended');

const StormSwathPayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


