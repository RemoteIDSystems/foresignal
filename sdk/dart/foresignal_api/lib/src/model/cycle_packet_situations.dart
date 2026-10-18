//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/cycle_packet_item.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cycle_packet_situations.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CyclePacketSituations {
  /// Returns a new [CyclePacketSituations] instance.
  CyclePacketSituations({

     this.new_,

     this.activated,

     this.escalated,

     this.dormant,

     this.closed,
  });

  @JsonKey(
    
    name: r'new',
    required: false,
    includeIfNull: false,
  )


  final List<CyclePacketItem>? new_;



  @JsonKey(
    
    name: r'activated',
    required: false,
    includeIfNull: false,
  )


  final List<CyclePacketItem>? activated;



  @JsonKey(
    
    name: r'escalated',
    required: false,
    includeIfNull: false,
  )


  final List<CyclePacketItem>? escalated;



  @JsonKey(
    
    name: r'dormant',
    required: false,
    includeIfNull: false,
  )


  final List<CyclePacketItem>? dormant;



  @JsonKey(
    
    name: r'closed',
    required: false,
    includeIfNull: false,
  )


  final List<CyclePacketItem>? closed;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CyclePacketSituations &&
      other.new_ == new_ &&
      other.activated == activated &&
      other.escalated == escalated &&
      other.dormant == dormant &&
      other.closed == closed;

    @override
    int get hashCode =>
        new_.hashCode +
        activated.hashCode +
        escalated.hashCode +
        dormant.hashCode +
        closed.hashCode;

  factory CyclePacketSituations.fromJson(Map<String, dynamic> json) => _$CyclePacketSituationsFromJson(json);

  Map<String, dynamic> toJson() => _$CyclePacketSituationsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

