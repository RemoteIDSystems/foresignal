//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'search_reindex_state.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SearchReindexState {
  /// Returns a new [SearchReindexState] instance.
  SearchReindexState({

    required  this.state,

     this.fromModel,

     this.toModel,

     this.total,

     this.done,

     this.percent,

     this.ratePerMinute,

     this.etaSeconds,

     this.startedAt,
  });

  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final SearchReindexStateStateEnum state;



      /// Outgoing model id `<providerId>:<model>:<dimensions>`
  @JsonKey(
    
    name: r'fromModel',
    required: false,
    includeIfNull: false,
  )


  final String? fromModel;



  @JsonKey(
    
    name: r'toModel',
    required: false,
    includeIfNull: false,
  )


  final String? toModel;



  @JsonKey(
    
    name: r'total',
    required: false,
    includeIfNull: false,
  )


  final int? total;



  @JsonKey(
    
    name: r'done',
    required: false,
    includeIfNull: false,
  )


  final int? done;



          // minimum: 0
          // maximum: 100
  @JsonKey(
    
    name: r'percent',
    required: false,
    includeIfNull: false,
  )


  final num? percent;



      /// 10-minute moving average
  @JsonKey(
    
    name: r'ratePerMinute',
    required: false,
    includeIfNull: false,
  )


  final num? ratePerMinute;



  @JsonKey(
    
    name: r'etaSeconds',
    required: false,
    includeIfNull: false,
  )


  final int? etaSeconds;



  @JsonKey(
    
    name: r'startedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? startedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SearchReindexState &&
      other.state == state &&
      other.fromModel == fromModel &&
      other.toModel == toModel &&
      other.total == total &&
      other.done == done &&
      other.percent == percent &&
      other.ratePerMinute == ratePerMinute &&
      other.etaSeconds == etaSeconds &&
      other.startedAt == startedAt;

    @override
    int get hashCode =>
        state.hashCode +
        (fromModel == null ? 0 : fromModel.hashCode) +
        (toModel == null ? 0 : toModel.hashCode) +
        (total == null ? 0 : total.hashCode) +
        (done == null ? 0 : done.hashCode) +
        (percent == null ? 0 : percent.hashCode) +
        (ratePerMinute == null ? 0 : ratePerMinute.hashCode) +
        (etaSeconds == null ? 0 : etaSeconds.hashCode) +
        (startedAt == null ? 0 : startedAt.hashCode);

  factory SearchReindexState.fromJson(Map<String, dynamic> json) => _$SearchReindexStateFromJson(json);

  Map<String, dynamic> toJson() => _$SearchReindexStateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SearchReindexStateStateEnum {
@JsonValue(r'idle')
idle(r'idle'),
@JsonValue(r'backfilling')
backfilling(r'backfilling'),
@JsonValue(r'indexing')
indexing(r'indexing'),
@JsonValue(r'swapping')
swapping(r'swapping');

const SearchReindexStateStateEnum(this.value);

final String value;

@override
String toString() => value;
}


