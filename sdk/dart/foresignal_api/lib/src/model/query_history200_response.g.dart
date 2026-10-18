// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'query_history200_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$QueryHistory200ResponseCWProxy {
  QueryHistory200Response items(List<RecordVersion> items);

  QueryHistory200Response nextCursor(String? nextCursor);

  QueryHistory200Response attributions(List<Attribution>? attributions);

  QueryHistory200Response licenceFiltered(int? licenceFiltered);

  QueryHistory200Response count(int count);

  QueryHistory200Response exact(bool exact);

  QueryHistory200Response watermarkSeq(int watermarkSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `QueryHistory200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// QueryHistory200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  QueryHistory200Response call({
    List<RecordVersion> items,
    String? nextCursor,
    List<Attribution>? attributions,
    int? licenceFiltered,
    int count,
    bool exact,
    int watermarkSeq,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfQueryHistory200Response.copyWith(...)` or call `instanceOfQueryHistory200Response.copyWith.fieldName(value)` for a single field.
class _$QueryHistory200ResponseCWProxyImpl
    implements _$QueryHistory200ResponseCWProxy {
  const _$QueryHistory200ResponseCWProxyImpl(this._value);

  final QueryHistory200Response _value;

  @override
  QueryHistory200Response items(List<RecordVersion> items) =>
      call(items: items);

  @override
  QueryHistory200Response nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  @override
  QueryHistory200Response attributions(List<Attribution>? attributions) =>
      call(attributions: attributions);

  @override
  QueryHistory200Response licenceFiltered(int? licenceFiltered) =>
      call(licenceFiltered: licenceFiltered);

  @override
  QueryHistory200Response count(int count) => call(count: count);

  @override
  QueryHistory200Response exact(bool exact) => call(exact: exact);

  @override
  QueryHistory200Response watermarkSeq(int watermarkSeq) =>
      call(watermarkSeq: watermarkSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `QueryHistory200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// QueryHistory200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  QueryHistory200Response call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
    Object? attributions = const $CopyWithPlaceholder(),
    Object? licenceFiltered = const $CopyWithPlaceholder(),
    Object? count = const $CopyWithPlaceholder(),
    Object? exact = const $CopyWithPlaceholder(),
    Object? watermarkSeq = const $CopyWithPlaceholder(),
  }) {
    return QueryHistory200Response(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<RecordVersion>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
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
      watermarkSeq:
          watermarkSeq == const $CopyWithPlaceholder() || watermarkSeq == null
          ? _value.watermarkSeq
          // ignore: cast_nullable_to_non_nullable
          : watermarkSeq as int,
    );
  }
}

extension $QueryHistory200ResponseCopyWith on QueryHistory200Response {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfQueryHistory200Response.copyWith(...)` or `instanceOfQueryHistory200Response.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$QueryHistory200ResponseCWProxy get copyWith =>
      _$QueryHistory200ResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

QueryHistory200Response _$QueryHistory200ResponseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('QueryHistory200Response', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['items', 'count', 'exact', 'watermarkSeq'],
  );
  final val = QueryHistory200Response(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => RecordVersion.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
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
    watermarkSeq: $checkedConvert('watermarkSeq', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$QueryHistory200ResponseToJson(
  QueryHistory200Response instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
  'attributions': ?instance.attributions?.map((e) => e.toJson()).toList(),
  'licenceFiltered': ?instance.licenceFiltered,
  'count': instance.count,
  'exact': instance.exact,
  'watermarkSeq': instance.watermarkSeq,
};
