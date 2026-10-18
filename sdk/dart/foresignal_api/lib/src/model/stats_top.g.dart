// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'stats_top.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StatsTopCWProxy {
  StatsTop by(StatsTopByEnum by);

  StatsTop measure(String measure);

  StatsTop from(DateTime? from);

  StatsTop to(DateTime? to);

  StatsTop backend(StatsTopBackendEnum? backend);

  StatsTop items(List<StatsTopItemsInner> items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsTop(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsTop(...).copyWith(id: 12, name: "My name")
  /// ```
  StatsTop call({
    StatsTopByEnum by,
    String measure,
    DateTime? from,
    DateTime? to,
    StatsTopBackendEnum? backend,
    List<StatsTopItemsInner> items,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStatsTop.copyWith(...)` or call `instanceOfStatsTop.copyWith.fieldName(value)` for a single field.
class _$StatsTopCWProxyImpl implements _$StatsTopCWProxy {
  const _$StatsTopCWProxyImpl(this._value);

  final StatsTop _value;

  @override
  StatsTop by(StatsTopByEnum by) => call(by: by);

  @override
  StatsTop measure(String measure) => call(measure: measure);

  @override
  StatsTop from(DateTime? from) => call(from: from);

  @override
  StatsTop to(DateTime? to) => call(to: to);

  @override
  StatsTop backend(StatsTopBackendEnum? backend) => call(backend: backend);

  @override
  StatsTop items(List<StatsTopItemsInner> items) => call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsTop(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsTop(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StatsTop call({
    Object? by = const $CopyWithPlaceholder(),
    Object? measure = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? backend = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
  }) {
    return StatsTop(
      by: by == const $CopyWithPlaceholder() || by == null
          ? _value.by
          // ignore: cast_nullable_to_non_nullable
          : by as StatsTopByEnum,
      measure: measure == const $CopyWithPlaceholder() || measure == null
          ? _value.measure
          // ignore: cast_nullable_to_non_nullable
          : measure as String,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime?,
      backend: backend == const $CopyWithPlaceholder()
          ? _value.backend
          // ignore: cast_nullable_to_non_nullable
          : backend as StatsTopBackendEnum?,
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<StatsTopItemsInner>,
    );
  }
}

extension $StatsTopCopyWith on StatsTop {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStatsTop.copyWith(...)` or `instanceOfStatsTop.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StatsTopCWProxy get copyWith => _$StatsTopCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StatsTop _$StatsTopFromJson(Map<String, dynamic> json) => $checkedCreate(
  'StatsTop',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['by', 'measure', 'items']);
    final val = StatsTop(
      by: $checkedConvert('by', (v) => $enumDecode(_$StatsTopByEnumEnumMap, v)),
      measure: $checkedConvert('measure', (v) => v as String),
      from: $checkedConvert(
        'from',
        (v) => v == null ? null : DateTime.parse(v as String),
      ),
      to: $checkedConvert(
        'to',
        (v) => v == null ? null : DateTime.parse(v as String),
      ),
      backend: $checkedConvert(
        'backend',
        (v) => $enumDecodeNullable(_$StatsTopBackendEnumEnumMap, v),
      ),
      items: $checkedConvert(
        'items',
        (v) => (v as List<dynamic>)
            .map((e) => StatsTopItemsInner.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
    );
    return val;
  },
);

Map<String, dynamic> _$StatsTopToJson(StatsTop instance) => <String, dynamic>{
  'by': _$StatsTopByEnumEnumMap[instance.by]!,
  'measure': instance.measure,
  'from': ?instance.from?.toIso8601String(),
  'to': ?instance.to?.toIso8601String(),
  'backend': ?_$StatsTopBackendEnumEnumMap[instance.backend],
  'items': instance.items.map((e) => e.toJson()).toList(),
};

const _$StatsTopByEnumEnumMap = {
  StatsTopByEnum.source_: 'source',
  StatsTopByEnum.kind: 'kind',
  StatsTopByEnum.cell: 'cell',
  StatsTopByEnum.entity: 'entity',
};

const _$StatsTopBackendEnumEnumMap = {
  StatsTopBackendEnum.rollup: 'rollup',
  StatsTopBackendEnum.raw: 'raw',
  StatsTopBackendEnum.mixed: 'mixed',
};
