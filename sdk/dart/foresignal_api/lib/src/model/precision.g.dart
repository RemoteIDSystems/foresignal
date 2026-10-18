// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precision.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecisionCWProxy {
  Precision since(DateTime? since);

  Precision generatedAt(DateTime generatedAt);

  Precision components(List<PrecisionComponentsInner> components);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Precision(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Precision(...).copyWith(id: 12, name: "My name")
  /// ```
  Precision call({
    DateTime? since,
    DateTime generatedAt,
    List<PrecisionComponentsInner> components,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecision.copyWith(...)` or call `instanceOfPrecision.copyWith.fieldName(value)` for a single field.
class _$PrecisionCWProxyImpl implements _$PrecisionCWProxy {
  const _$PrecisionCWProxyImpl(this._value);

  final Precision _value;

  @override
  Precision since(DateTime? since) => call(since: since);

  @override
  Precision generatedAt(DateTime generatedAt) => call(generatedAt: generatedAt);

  @override
  Precision components(List<PrecisionComponentsInner> components) =>
      call(components: components);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Precision(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Precision(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Precision call({
    Object? since = const $CopyWithPlaceholder(),
    Object? generatedAt = const $CopyWithPlaceholder(),
    Object? components = const $CopyWithPlaceholder(),
  }) {
    return Precision(
      since: since == const $CopyWithPlaceholder()
          ? _value.since
          // ignore: cast_nullable_to_non_nullable
          : since as DateTime?,
      generatedAt:
          generatedAt == const $CopyWithPlaceholder() || generatedAt == null
          ? _value.generatedAt
          // ignore: cast_nullable_to_non_nullable
          : generatedAt as DateTime,
      components:
          components == const $CopyWithPlaceholder() || components == null
          ? _value.components
          // ignore: cast_nullable_to_non_nullable
          : components as List<PrecisionComponentsInner>,
    );
  }
}

extension $PrecisionCopyWith on Precision {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecision.copyWith(...)` or `instanceOfPrecision.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecisionCWProxy get copyWith => _$PrecisionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Precision _$PrecisionFromJson(Map<String, dynamic> json) => $checkedCreate(
  'Precision',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['generatedAt', 'components']);
    final val = Precision(
      since: $checkedConvert(
        'since',
        (v) => v == null ? null : DateTime.parse(v as String),
      ),
      generatedAt: $checkedConvert(
        'generatedAt',
        (v) => DateTime.parse(v as String),
      ),
      components: $checkedConvert(
        'components',
        (v) => (v as List<dynamic>)
            .map(
              (e) =>
                  PrecisionComponentsInner.fromJson(e as Map<String, dynamic>),
            )
            .toList(),
      ),
    );
    return val;
  },
);

Map<String, dynamic> _$PrecisionToJson(Precision instance) => <String, dynamic>{
  'since': ?instance.since?.toIso8601String(),
  'generatedAt': instance.generatedAt.toIso8601String(),
  'components': instance.components.map((e) => e.toJson()).toList(),
};
