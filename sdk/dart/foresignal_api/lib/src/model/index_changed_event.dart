//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'index_changed_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IndexChangedEvent {
  /// Returns a new [IndexChangedEvent] instance.
  IndexChangedEvent({

    required  this.indexId,

    required  this.region,

    required  this.day,

    required  this.value,

     this.previousValue,

     this.delta24h,

     this.delta7d,

     this.weightsOrigin,
  });

  @JsonKey(
    
    name: r'indexId',
    required: true,
    includeIfNull: false,
  )


  final String indexId;



  @JsonKey(
    
    name: r'region',
    required: true,
    includeIfNull: false,
  )


  final String region;



  @JsonKey(
    
    name: r'day',
    required: true,
    includeIfNull: false,
  )


  final DateTime day;



  @JsonKey(
    
    name: r'value',
    required: true,
    includeIfNull: false,
  )


  final num value;



  @JsonKey(
    
    name: r'previousValue',
    required: false,
    includeIfNull: false,
  )


  final num? previousValue;



  @JsonKey(
    
    name: r'delta24h',
    required: false,
    includeIfNull: false,
  )


  final num? delta24h;



  @JsonKey(
    
    name: r'delta7d',
    required: false,
    includeIfNull: false,
  )


  final num? delta7d;



  @JsonKey(
    
    name: r'weightsOrigin',
    required: false,
    includeIfNull: false,
  )


  final IndexChangedEventWeightsOriginEnum? weightsOrigin;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IndexChangedEvent &&
      other.indexId == indexId &&
      other.region == region &&
      other.day == day &&
      other.value == value &&
      other.previousValue == previousValue &&
      other.delta24h == delta24h &&
      other.delta7d == delta7d &&
      other.weightsOrigin == weightsOrigin;

    @override
    int get hashCode =>
        indexId.hashCode +
        region.hashCode +
        day.hashCode +
        value.hashCode +
        (previousValue == null ? 0 : previousValue.hashCode) +
        (delta24h == null ? 0 : delta24h.hashCode) +
        (delta7d == null ? 0 : delta7d.hashCode) +
        weightsOrigin.hashCode;

  factory IndexChangedEvent.fromJson(Map<String, dynamic> json) => _$IndexChangedEventFromJson(json);

  Map<String, dynamic> toJson() => _$IndexChangedEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IndexChangedEventWeightsOriginEnum {
@JsonValue(r'default')
default_(r'default'),
@JsonValue(r'fitted')
fitted(r'fitted'),
@JsonValue(r'operator')
operator_(r'operator');

const IndexChangedEventWeightsOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


