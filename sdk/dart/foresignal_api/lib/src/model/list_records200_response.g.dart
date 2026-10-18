// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'list_records200_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ListRecords200ResponseCWProxy {
  ListRecords200Response items(List<Record> items);

  ListRecords200Response nextCursor(String? nextCursor);

  ListRecords200Response watermarkSeq(int watermarkSeq);

  ListRecords200Response asOfSeq(int? asOfSeq);

  ListRecords200Response asOf(DateTime? asOf);

  ListRecords200Response approximate(bool? approximate);

  ListRecords200Response horizon(DateTime? horizon);

  ListRecords200Response attributions(List<Attribution>? attributions);

  ListRecords200Response licenceFiltered(int? licenceFiltered);

  ListRecords200Response count(int count);

  ListRecords200Response exact(bool exact);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ListRecords200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ListRecords200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  ListRecords200Response call({
    List<Record> items,
    String? nextCursor,
    int watermarkSeq,
    int? asOfSeq,
    DateTime? asOf,
    bool? approximate,
    DateTime? horizon,
    List<Attribution>? attributions,
    int? licenceFiltered,
    int count,
    bool exact,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfListRecords200Response.copyWith(...)` or call `instanceOfListRecords200Response.copyWith.fieldName(value)` for a single field.
class _$ListRecords200ResponseCWProxyImpl
    implements _$ListRecords200ResponseCWProxy {
  const _$ListRecords200ResponseCWProxyImpl(this._value);

  final ListRecords200Response _value;

  @override
  ListRecords200Response items(List<Record> items) => call(items: items);

  @override
  ListRecords200Response nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  @override
  ListRecords200Response watermarkSeq(int watermarkSeq) =>
      call(watermarkSeq: watermarkSeq);

  @override
  ListRecords200Response asOfSeq(int? asOfSeq) => call(asOfSeq: asOfSeq);

  @override
  ListRecords200Response asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  ListRecords200Response approximate(bool? approximate) =>
      call(approximate: approximate);

  @override
  ListRecords200Response horizon(DateTime? horizon) => call(horizon: horizon);

  @override
  ListRecords200Response attributions(List<Attribution>? attributions) =>
      call(attributions: attributions);

  @override
  ListRecords200Response licenceFiltered(int? licenceFiltered) =>
      call(licenceFiltered: licenceFiltered);

  @override
  ListRecords200Response count(int count) => call(count: count);

  @override
  ListRecords200Response exact(bool exact) => call(exact: exact);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ListRecords200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ListRecords200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ListRecords200Response call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
    Object? watermarkSeq = const $CopyWithPlaceholder(),
    Object? asOfSeq = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? approximate = const $CopyWithPlaceholder(),
    Object? horizon = const $CopyWithPlaceholder(),
    Object? attributions = const $CopyWithPlaceholder(),
    Object? licenceFiltered = const $CopyWithPlaceholder(),
    Object? count = const $CopyWithPlaceholder(),
    Object? exact = const $CopyWithPlaceholder(),
  }) {
    return ListRecords200Response(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Record>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
      watermarkSeq:
          watermarkSeq == const $CopyWithPlaceholder() || watermarkSeq == null
          ? _value.watermarkSeq
          // ignore: cast_nullable_to_non_nullable
          : watermarkSeq as int,
      asOfSeq: asOfSeq == const $CopyWithPlaceholder()
          ? _value.asOfSeq
          // ignore: cast_nullable_to_non_nullable
          : asOfSeq as int?,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
      approximate: approximate == const $CopyWithPlaceholder()
          ? _value.approximate
          // ignore: cast_nullable_to_non_nullable
          : approximate as bool?,
      horizon: horizon == const $CopyWithPlaceholder()
          ? _value.horizon
          // ignore: cast_nullable_to_non_nullable
          : horizon as DateTime?,
      attributions: attributions == const $CopyWithPlaceholder()
          ? _value.attributions
          // ignore: cast_nullable_to_non_nullable
          : attributions as List<Attribution>?,
      licenceFiltered: licenceFiltered == const $CopyWithPlaceholder()
          ? _value.licenceFiltered
          // ignore: cast_nullable_to_non_nullable
          : licenceFiltered as int?,
      count: count == const $CopyWithPlaceholder() || count == null
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int,
      exact: exact == const $CopyWithPlaceholder() || exact == null
          ? _value.exact
          // ignore: cast_nullable_to_non_nullable
          : exact as bool,
    );
  }
}

extension $ListRecords200ResponseCopyWith on ListRecords200Response {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfListRecords200Response.copyWith(...)` or `instanceOfListRecords200Response.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ListRecords200ResponseCWProxy get copyWith =>
      _$ListRecords200ResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ListRecords200Response _$ListRecords200ResponseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ListRecords200Response', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['items', 'watermarkSeq', 'count', 'exact'],
  );
  final val = ListRecords200Response(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => Record.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
    watermarkSeq: $checkedConvert('watermarkSeq', (v) => (v as num).toInt()),
    asOfSeq: $checkedConvert('asOfSeq', (v) => (v as num?)?.toInt()),
    asOf: $checkedConvert(
      'asOf',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    approximate: $checkedConvert('approximate', (v) => v as bool?),
    horizon: $checkedConvert(
      'horizon',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    attributions: $checkedConvert(
      'attributions',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Attribution.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    licenceFiltered: $checkedConvert(
      'licenceFiltered',
      (v) => (v as num?)?.toInt(),
    ),
    count: $checkedConvert('count', (v) => (v as num).toInt()),
    exact: $checkedConvert('exact', (v) => v as bool),
  );
  return val;
});

Map<String, dynamic> _$ListRecords200ResponseToJson(
  ListRecords200Response instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
  'watermarkSeq': instance.watermarkSeq,
  'asOfSeq': ?instance.asOfSeq,
  'asOf': ?instance.asOf?.toIso8601String(),
  'approximate': ?instance.approximate,
  'horizon': ?instance.horizon?.toIso8601String(),
  'attributions': ?instance.attributions?.map((e) => e.toJson()).toList(),
  'licenceFiltered': ?instance.licenceFiltered,
  'count': instance.count,
  'exact': instance.exact,
};
