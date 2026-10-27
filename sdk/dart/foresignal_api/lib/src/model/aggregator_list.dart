//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/aggregator.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'aggregator_list.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AggregatorList {
  /// Returns a new [AggregatorList] instance.
  AggregatorList({

    required  this.items,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<Aggregator> items;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AggregatorList &&
      other.items == items;

    @override
    int get hashCode =>
        items.hashCode;

  factory AggregatorList.fromJson(Map<String, dynamic> json) => _$AggregatorListFromJson(json);

  Map<String, dynamic> toJson() => _$AggregatorListToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

