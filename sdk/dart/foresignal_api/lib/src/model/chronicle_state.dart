//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/chronicle_state_layers_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_state.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleState {
  /// Returns a new [ChronicleState] instance.
  ChronicleState({

    required  this.chronicleId,

    required  this.at,

    required  this.layers,

     this.caveats,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'chronicleId',
    required: true,
    includeIfNull: false,
  )


  final String chronicleId;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



  @JsonKey(
    
    name: r'layers',
    required: true,
    includeIfNull: false,
  )


  final List<ChronicleStateLayersInner> layers;



  @JsonKey(
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? caveats;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleState &&
      other.chronicleId == chronicleId &&
      other.at == at &&
      other.layers == layers &&
      other.caveats == caveats;

    @override
    int get hashCode =>
        chronicleId.hashCode +
        at.hashCode +
        layers.hashCode +
        caveats.hashCode;

  factory ChronicleState.fromJson(Map<String, dynamic> json) => _$ChronicleStateFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleStateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

