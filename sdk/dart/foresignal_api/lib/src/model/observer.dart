//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'observer.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Observer {
  /// Returns a new [Observer] instance.
  Observer({

    required  this.receiverId,

     this.frequencyHz,

     this.rssi,

     this.snr,

     this.decodeQuality,
  });

  @JsonKey(
    
    name: r'receiverId',
    required: true,
    includeIfNull: false,
  )


  final String receiverId;



  @JsonKey(
    
    name: r'frequencyHz',
    required: false,
    includeIfNull: false,
  )


  final int? frequencyHz;



  @JsonKey(
    
    name: r'rssi',
    required: false,
    includeIfNull: false,
  )


  final num? rssi;



  @JsonKey(
    
    name: r'snr',
    required: false,
    includeIfNull: false,
  )


  final num? snr;



  @JsonKey(
    
    name: r'decodeQuality',
    required: false,
    includeIfNull: false,
  )


  final num? decodeQuality;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Observer &&
      other.receiverId == receiverId &&
      other.frequencyHz == frequencyHz &&
      other.rssi == rssi &&
      other.snr == snr &&
      other.decodeQuality == decodeQuality;

    @override
    int get hashCode =>
        receiverId.hashCode +
        (frequencyHz == null ? 0 : frequencyHz.hashCode) +
        (rssi == null ? 0 : rssi.hashCode) +
        (snr == null ? 0 : snr.hashCode) +
        (decodeQuality == null ? 0 : decodeQuality.hashCode);

  factory Observer.fromJson(Map<String, dynamic> json) => _$ObserverFromJson(json);

  Map<String, dynamic> toJson() => _$ObserverToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

