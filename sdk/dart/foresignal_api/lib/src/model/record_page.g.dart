// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'record_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RecordPageCWProxy {
  RecordPage items(List<Record> items);

  RecordPage nextCursor(String? nextCursor);

  RecordPage watermarkSeq(int? watermarkSeq);

  RecordPage asOfSeq(int? asOfSeq);

  RecordPage asOf(DateTime? asOf);

  RecordPage approximate(bool? approximate);

  RecordPage horizon(DateTime? horizon);

  RecordPage attributions(List<Attribution>? attributions);

  RecordPage licenceFiltered(int? licenceFiltered);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordPage(...).copyWith(id: 12, name: "My name")
  /// ```
  RecordPage call({
    List<Record> items,
    String? nextCursor,
    int? watermarkSeq,
    int? asOfSeq,
    DateTime? asOf,
    bool? approximate,
    DateTime? horizon,
    List<Attribution>? attributions,
    int? licenceFiltered,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRecordPage.copyWith(...)` or call `instanceOfRecordPage.copyWith.fieldName(value)` for a single field.
class _$RecordPageCWProxyImpl implements _$RecordPageCWProxy {
  const _$RecordPageCWProxyImpl(this._value);

  final RecordPage _value;

  @override
  RecordPage items(List<Record> items) => call(items: items);

  @override
  RecordPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  @override
  RecordPage watermarkSeq(int? watermarkSeq) =>
      call(watermarkSeq: watermarkSeq);

  @override
  RecordPage asOfSeq(int? asOfSeq) => call(asOfSeq: asOfSeq);

  @override
  RecordPage asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  RecordPage approximate(bool? approximate) => call(approximate: approximate);

  @override
  RecordPage horizon(DateTime? horizon) => call(horizon: horizon);

  @override
  RecordPage attributions(List<Attribution>? attributions) =>
      call(attributions: attributions);

  @override
  RecordPage licenceFiltered(int? licenceFiltered) =>
      call(licenceFiltered: licenceFiltered);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RecordPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
    Object? watermarkSeq = const $CopyWithPlaceholder(),
    Object? asOfSeq = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? approximate = const $CopyWithPlaceholder(),
    Object? horizon = const $CopyWithPlaceholder(),
    Object? attributions = const $CopyWithPlaceholder(),
    Object? licenceFiltered = const $CopyWithPlaceholder(),
  }) {
    return RecordPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Record>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
      watermarkSeq: watermarkSeq == const $CopyWithPlaceholder()
          ? _value.watermarkSeq
          // ignore: cast_nullable_to_non_nullable
          : watermarkSeq as int?,
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
    );
  }
}

extension $RecordPageCopyWith on RecordPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRecordPage.copyWith(...)` or `instanceOfRecordPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RecordPageCWProxy get copyWith => _$RecordPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RecordPage _$RecordPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RecordPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = RecordPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Record.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
        watermarkSeq: $checkedConvert(
          'watermarkSeq',
          (v) => (v as num?)?.toInt(),
        ),
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
      );
      return val;
    });

Map<String, dynamic> _$RecordPageToJson(RecordPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
      'watermarkSeq': ?instance.watermarkSeq,
      'asOfSeq': ?instance.asOfSeq,
      'asOf': ?instance.asOf?.toIso8601String(),
      'approximate': ?instance.approximate,
      'horizon': ?instance.horizon?.toIso8601String(),
      'attributions': ?instance.attributions?.map((e) => e.toJson()).toList(),
      'licenceFiltered': ?instance.licenceFiltered,
    };
