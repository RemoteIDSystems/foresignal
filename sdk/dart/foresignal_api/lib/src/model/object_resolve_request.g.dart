// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'object_resolve_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ObjectResolveRequestCWProxy {
  ObjectResolveRequest refs(List<String> refs);

  ObjectResolveRequest asOf(DateTime? asOf);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectResolveRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectResolveRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  ObjectResolveRequest call({List<String> refs, DateTime? asOf});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfObjectResolveRequest.copyWith(...)` or call `instanceOfObjectResolveRequest.copyWith.fieldName(value)` for a single field.
class _$ObjectResolveRequestCWProxyImpl
    implements _$ObjectResolveRequestCWProxy {
  const _$ObjectResolveRequestCWProxyImpl(this._value);

  final ObjectResolveRequest _value;

  @override
  ObjectResolveRequest refs(List<String> refs) => call(refs: refs);

  @override
  ObjectResolveRequest asOf(DateTime? asOf) => call(asOf: asOf);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectResolveRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectResolveRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ObjectResolveRequest call({
    Object? refs = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
  }) {
    return ObjectResolveRequest(
      refs: refs == const $CopyWithPlaceholder() || refs == null
          ? _value.refs
          // ignore: cast_nullable_to_non_nullable
          : refs as List<String>,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
    );
  }
}

extension $ObjectResolveRequestCopyWith on ObjectResolveRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfObjectResolveRequest.copyWith(...)` or `instanceOfObjectResolveRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ObjectResolveRequestCWProxy get copyWith =>
      _$ObjectResolveRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ObjectResolveRequest _$ObjectResolveRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ObjectResolveRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['refs']);
  final val = ObjectResolveRequest(
    refs: $checkedConvert(
      'refs',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    asOf: $checkedConvert(
      'asOf',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$ObjectResolveRequestToJson(
  ObjectResolveRequest instance,
) => <String, dynamic>{
  'refs': instance.refs,
  'asOf': ?instance.asOf?.toIso8601String(),
};
