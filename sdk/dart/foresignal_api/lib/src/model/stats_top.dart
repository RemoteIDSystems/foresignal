//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/stats_top_items_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'stats_top.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StatsTop {
  /// Returns a new [StatsTop] instance.
  StatsTop({

    required  this.by,

    required  this.measure,

     this.from,

     this.to,

     this.backend,

    required  this.items,
  });

  @JsonKey(
    
    name: r'by',
    required: true,
    includeIfNull: false,
  )


  final StatsTopByEnum by;



  @JsonKey(
    
    name: r'measure',
    required: true,
    includeIfNull: false,
  )


  final String measure;



  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  )


  final DateTime? from;



  @JsonKey(
    
    name: r'to',
    required: false,
    includeIfNull: false,
  )


  final DateTime? to;



  @JsonKey(
    
    name: r'backend',
    required: false,
    includeIfNull: false,
  )


  final StatsTopBackendEnum? backend;



  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<StatsTopItemsInner> items;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StatsTop &&
      other.by == by &&
      other.measure == measure &&
      other.from == from &&
      other.to == to &&
      other.backend == backend &&
      other.items == items;

    @override
    int get hashCode =>
        by.hashCode +
        measure.hashCode +
        from.hashCode +
        to.hashCode +
        backend.hashCode +
        items.hashCode;

  factory StatsTop.fromJson(Map<String, dynamic> json) => _$StatsTopFromJson(json);

  Map<String, dynamic> toJson() => _$StatsTopToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StatsTopByEnum {
@JsonValue(r'source')
source_(r'source'),
@JsonValue(r'kind')
kind(r'kind'),
@JsonValue(r'cell')
cell(r'cell'),
@JsonValue(r'entity')
entity(r'entity');

const StatsTopByEnum(this.value);

final String value;

@override
String toString() => value;
}


enum StatsTopBackendEnum {
@JsonValue(r'rollup')
rollup(r'rollup'),
@JsonValue(r'raw')
raw(r'raw'),
@JsonValue(r'mixed')
mixed(r'mixed');

const StatsTopBackendEnum(this.value);

final String value;

@override
String toString() => value;
}


