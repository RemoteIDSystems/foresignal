//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/change.dart';
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'change_page.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChangePage {
  /// Returns a new [ChangePage] instance.
  ChangePage({

    required  this.items,

    required  this.lastSeq,

    required  this.hasMore,

     this.watermarkSeq,

     this.purgeHorizon,

     this.attributions,

     this.licenceFiltered,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<Change> items;



      /// Pass as `after` next time. When `hasMore` is false it is at least the `watermarkSeq` read before the query, so a filtered client skips past seqs that matched nothing
  @JsonKey(
    
    name: r'lastSeq',
    required: true,
    includeIfNull: false,
  )


  final int lastSeq;



  @JsonKey(
    
    name: r'hasMore',
    required: true,
    includeIfNull: false,
  )


  final bool hasMore;



      /// ChangeBus.lastPublishedSeq() at query time
  @JsonKey(
    
    name: r'watermarkSeq',
    required: false,
    includeIfNull: false,
  )


  final int? watermarkSeq;



      /// Max RetentionState.purgeSeq over the filtered sources; after < purgeHorizon → 410 RESYNC_REQUIRED
  @JsonKey(
    
    name: r'purgeHorizon',
    required: false,
    includeIfNull: false,
  )


  final int? purgeHorizon;



  @JsonKey(
    
    name: r'attributions',
    required: false,
    includeIfNull: false,
  )


  final List<Attribution>? attributions;



          // minimum: 0
  @JsonKey(
    
    name: r'licenceFiltered',
    required: false,
    includeIfNull: false,
  )


  final int? licenceFiltered;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChangePage &&
      other.items == items &&
      other.lastSeq == lastSeq &&
      other.hasMore == hasMore &&
      other.watermarkSeq == watermarkSeq &&
      other.purgeHorizon == purgeHorizon &&
      other.attributions == attributions &&
      other.licenceFiltered == licenceFiltered;

    @override
    int get hashCode =>
        items.hashCode +
        lastSeq.hashCode +
        hasMore.hashCode +
        watermarkSeq.hashCode +
        purgeHorizon.hashCode +
        attributions.hashCode +
        licenceFiltered.hashCode;

  factory ChangePage.fromJson(Map<String, dynamic> json) => _$ChangePageFromJson(json);

  Map<String, dynamic> toJson() => _$ChangePageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

