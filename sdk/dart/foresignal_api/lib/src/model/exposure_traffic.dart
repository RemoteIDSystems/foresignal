//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exposure_traffic.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExposureTraffic {
  /// Returns a new [ExposureTraffic] instance.
  ExposureTraffic({

    required  this.status,

     this.attribution,

     this.aircraft,

     this.vessels,
  });

  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ExposureTrafficStatusEnum status;



  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final List<Attribution>? attribution;



  @JsonKey(
    
    name: r'aircraft',
    required: false,
    includeIfNull: false,
  )


  final int? aircraft;



  @JsonKey(
    
    name: r'vessels',
    required: false,
    includeIfNull: false,
  )


  final int? vessels;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExposureTraffic &&
      other.status == status &&
      other.attribution == attribution &&
      other.aircraft == aircraft &&
      other.vessels == vessels;

    @override
    int get hashCode =>
        status.hashCode +
        attribution.hashCode +
        aircraft.hashCode +
        vessels.hashCode;

  factory ExposureTraffic.fromJson(Map<String, dynamic> json) => _$ExposureTrafficFromJson(json);

  Map<String, dynamic> toJson() => _$ExposureTrafficToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExposureTrafficStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'unavailable')
unavailable(r'unavailable'),
@JsonValue(r'not-installed')
notInstalled(r'not-installed');

const ExposureTrafficStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


