//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/record_version.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'history_page.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HistoryPage {
  /// Returns a new [HistoryPage] instance.
  HistoryPage({

    required  this.items,

     this.nextCursor,

     this.attributions,

     this.licenceFiltered,
  });

      /// Versions ordered by the selected timeField
  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<RecordVersion> items;



  @JsonKey(
    
    name: r'nextCursor',
    required: false,
    includeIfNull: false,
  )


  final String? nextCursor;



  @JsonKey(
    
    name: r'attributions',
    required: false,
    includeIfNull: false,
  )


  final List<Attribution>? attributions;



      /// Records removed by the licence policy
          // minimum: 0
  @JsonKey(
    
    name: r'licenceFiltered',
    required: false,
    includeIfNull: false,
  )


  final int? licenceFiltered;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HistoryPage &&
      other.items == items &&
      other.nextCursor == nextCursor &&
      other.attributions == attributions &&
      other.licenceFiltered == licenceFiltered;

    @override
    int get hashCode =>
        items.hashCode +
        (nextCursor == null ? 0 : nextCursor.hashCode) +
        attributions.hashCode +
        licenceFiltered.hashCode;

  factory HistoryPage.fromJson(Map<String, dynamic> json) => _$HistoryPageFromJson(json);

  Map<String, dynamic> toJson() => _$HistoryPageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

