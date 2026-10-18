// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'observability_coverage.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ObservabilityCoverageCWProxy {
  ObservabilityCoverage family(String family);

  ObservabilityCoverage at(DateTime at);

  ObservabilityCoverage band(String? band);

  ObservabilityCoverage absenceMinDetect(num absenceMinDetect);

  ObservabilityCoverage items(List<Detectability> items);

  ObservabilityCoverage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObservabilityCoverage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObservabilityCoverage(...).copyWith(id: 12, name: "My name")
  /// ```
  ObservabilityCoverage call({
    String family,
    DateTime at,
    String? band,
    num absenceMinDetect,
    List<Detectability> items,
    String? nextCursor,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfObservabilityCoverage.copyWith(...)` or call `instanceOfObservabilityCoverage.copyWith.fieldName(value)` for a single field.
class _$ObservabilityCoverageCWProxyImpl
    implements _$ObservabilityCoverageCWProxy {
  const _$ObservabilityCoverageCWProxyImpl(this._value);

  final ObservabilityCoverage _value;

  @override
  ObservabilityCoverage family(String family) => call(family: family);

  @override
  ObservabilityCoverage at(DateTime at) => call(at: at);

  @override
  ObservabilityCoverage band(String? band) => call(band: band);

  @override
  ObservabilityCoverage absenceMinDetect(num absenceMinDetect) =>
      call(absenceMinDetect: absenceMinDetect);

  @override
  ObservabilityCoverage items(List<Detectability> items) => call(items: items);

  @override
  ObservabilityCoverage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObservabilityCoverage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObservabilityCoverage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ObservabilityCoverage call({
    Object? family = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
    Object? band = const $CopyWithPlaceholder(),
    Object? absenceMinDetect = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ObservabilityCoverage(
      family: family == const $CopyWithPlaceholder() || family == null
          ? _value.family
          // ignore: cast_nullable_to_non_nullable
          : family as String,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      band: band == const $CopyWithPlaceholder()
          ? _value.band
          // ignore: cast_nullable_to_non_nullable
          : band as String?,
      absenceMinDetect:
          absenceMinDetect == const $CopyWithPlaceholder() ||
              absenceMinDetect == null
          ? _value.absenceMinDetect
          // ignore: cast_nullable_to_non_nullable
          : absenceMinDetect as num,
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Detectability>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ObservabilityCoverageCopyWith on ObservabilityCoverage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfObservabilityCoverage.copyWith(...)` or `instanceOfObservabilityCoverage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ObservabilityCoverageCWProxy get copyWith =>
      _$ObservabilityCoverageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ObservabilityCoverage _$ObservabilityCoverageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ObservabilityCoverage', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['family', 'at', 'absenceMinDetect', 'items'],
  );
  final val = ObservabilityCoverage(
    family: $checkedConvert('family', (v) => v as String),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
    band: $checkedConvert('band', (v) => v as String?),
    absenceMinDetect: $checkedConvert('absenceMinDetect', (v) => v as num),
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => Detectability.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ObservabilityCoverageToJson(
  ObservabilityCoverage instance,
) => <String, dynamic>{
  'family': instance.family,
  'at': instance.at.toIso8601String(),
  'band': ?instance.band,
  'absenceMinDetect': instance.absenceMinDetect,
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
