// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'object_resolve_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ObjectResolveResultCWProxy {
  ObjectResolveResult items(List<ObjectEnvelope> items);

  ObjectResolveResult missing(List<String> missing);

  ObjectResolveResult asOf(DateTime? asOf);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectResolveResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectResolveResult(...).copyWith(id: 12, name: "My name")
  /// ```
  ObjectResolveResult call({
    List<ObjectEnvelope> items,
    List<String> missing,
    DateTime? asOf,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfObjectResolveResult.copyWith(...)` or call `instanceOfObjectResolveResult.copyWith.fieldName(value)` for a single field.
class _$ObjectResolveResultCWProxyImpl implements _$ObjectResolveResultCWProxy {
  const _$ObjectResolveResultCWProxyImpl(this._value);

  final ObjectResolveResult _value;

  @override
  ObjectResolveResult items(List<ObjectEnvelope> items) => call(items: items);

  @override
  ObjectResolveResult missing(List<String> missing) => call(missing: missing);

  @override
  ObjectResolveResult asOf(DateTime? asOf) => call(asOf: asOf);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectResolveResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectResolveResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ObjectResolveResult call({
    Object? items = const $CopyWithPlaceholder(),
    Object? missing = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
  }) {
    return ObjectResolveResult(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ObjectEnvelope>,
      missing: missing == const $CopyWithPlaceholder() || missing == null
          ? _value.missing
          // ignore: cast_nullable_to_non_nullable
          : missing as List<String>,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
    );
  }
}

extension $ObjectResolveResultCopyWith on ObjectResolveResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfObjectResolveResult.copyWith(...)` or `instanceOfObjectResolveResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ObjectResolveResultCWProxy get copyWith =>
      _$ObjectResolveResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ObjectResolveResult _$ObjectResolveResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ObjectResolveResult', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'missing']);
      final val = ObjectResolveResult(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => ObjectEnvelope.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        missing: $checkedConvert(
          'missing',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        asOf: $checkedConvert(
          'asOf',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ObjectResolveResultToJson(
  ObjectResolveResult instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'missing': instance.missing,
  'asOf': ?instance.asOf?.toIso8601String(),
};
