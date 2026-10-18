//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'claim_event_time.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ClaimEventTime {
  /// Returns a new [ClaimEventTime] instance.
  ClaimEventTime({

    required  this.from,

     this.to,

    required  this.precision,
  });

  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final DateTime from;



  @JsonKey(
    
    name: r'to',
    required: false,
    includeIfNull: false,
  )


  final DateTime? to;



      /// Resolution of the time expression, e.g. `minute`, `hour`, `day`
  @JsonKey(
    
    name: r'precision',
    required: true,
    includeIfNull: false,
  )


  final String precision;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ClaimEventTime &&
      other.from == from &&
      other.to == to &&
      other.precision == precision;

    @override
    int get hashCode =>
        from.hashCode +
        (to == null ? 0 : to.hashCode) +
        precision.hashCode;

  factory ClaimEventTime.fromJson(Map<String, dynamic> json) => _$ClaimEventTimeFromJson(json);

  Map<String, dynamic> toJson() => _$ClaimEventTimeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

