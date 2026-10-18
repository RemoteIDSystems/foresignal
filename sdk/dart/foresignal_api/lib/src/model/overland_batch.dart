//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/overland_batch_locations_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'overland_batch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OverlandBatch {
  /// Returns a new [OverlandBatch] instance.
  OverlandBatch({

    required  this.locations,

     this.trip,

     this.current,
  });

  @JsonKey(
    
    name: r'locations',
    required: true,
    includeIfNull: false,
  )


  final List<OverlandBatchLocationsInner> locations;



      /// Ignored
  @JsonKey(
    
    name: r'trip',
    required: false,
    includeIfNull: false,
  )


  final Object? trip;



      /// Ignored
  @JsonKey(
    
    name: r'current',
    required: false,
    includeIfNull: false,
  )


  final Object? current;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OverlandBatch &&
      other.locations == locations &&
      other.trip == trip &&
      other.current == current;

    @override
    int get hashCode =>
        locations.hashCode +
        (trip == null ? 0 : trip.hashCode) +
        (current == null ? 0 : current.hashCode);

  factory OverlandBatch.fromJson(Map<String, dynamic> json) => _$OverlandBatchFromJson(json);

  Map<String, dynamic> toJson() => _$OverlandBatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

