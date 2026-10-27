// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'aggregator_list.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AggregatorListCWProxy {
  AggregatorList items(List<Aggregator> items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AggregatorList(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AggregatorList(...).copyWith(id: 12, name: "My name")
  /// ```
  AggregatorList call({List<Aggregator> items});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAggregatorList.copyWith(...)` or call `instanceOfAggregatorList.copyWith.fieldName(value)` for a single field.
class _$AggregatorListCWProxyImpl implements _$AggregatorListCWProxy {
  const _$AggregatorListCWProxyImpl(this._value);

  final AggregatorList _value;

  @override
  AggregatorList items(List<Aggregator> items) => call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AggregatorList(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AggregatorList(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AggregatorList call({Object? items = const $CopyWithPlaceholder()}) {
    return AggregatorList(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Aggregator>,
    );
  }
}

extension $AggregatorListCopyWith on AggregatorList {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAggregatorList.copyWith(...)` or `instanceOfAggregatorList.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AggregatorListCWProxy get copyWith => _$AggregatorListCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AggregatorList _$AggregatorListFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AggregatorList', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = AggregatorList(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Aggregator.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AggregatorListToJson(AggregatorList instance) =>
    <String, dynamic>{'items': instance.items.map((e) => e.toJson()).toList()};
