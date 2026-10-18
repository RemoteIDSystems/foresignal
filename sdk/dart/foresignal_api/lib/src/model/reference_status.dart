//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/reference_dataset.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'reference_status.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReferenceStatus {
  /// Returns a new [ReferenceStatus] instance.
  ReferenceStatus({

    required  this.datasets,

     this.loading,

     this.attributions,

     this.boundaryNote,
  });

  @JsonKey(
    
    name: r'datasets',
    required: true,
    includeIfNull: false,
  )


  final List<ReferenceDataset> datasets;



  @JsonKey(
    
    name: r'loading',
    required: false,
    includeIfNull: false,
  )


  final bool? loading;



  @JsonKey(
    
    name: r'attributions',
    required: false,
    includeIfNull: false,
  )


  final List<String>? attributions;



      /// Natural Earth draws de facto boundaries
  @JsonKey(
    
    name: r'boundaryNote',
    required: false,
    includeIfNull: false,
  )


  final String? boundaryNote;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReferenceStatus &&
      other.datasets == datasets &&
      other.loading == loading &&
      other.attributions == attributions &&
      other.boundaryNote == boundaryNote;

    @override
    int get hashCode =>
        datasets.hashCode +
        loading.hashCode +
        attributions.hashCode +
        boundaryNote.hashCode;

  factory ReferenceStatus.fromJson(Map<String, dynamic> json) => _$ReferenceStatusFromJson(json);

  Map<String, dynamic> toJson() => _$ReferenceStatusToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

