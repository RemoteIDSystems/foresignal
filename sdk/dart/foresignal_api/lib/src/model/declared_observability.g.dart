// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'declared_observability.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DeclaredObservabilityCWProxy {
  DeclaredObservability kinds(
    Map<String, DeclaredObservabilityKindsValue> kinds,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DeclaredObservability(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DeclaredObservability(...).copyWith(id: 12, name: "My name")
  /// ```
  DeclaredObservability call({
    Map<String, DeclaredObservabilityKindsValue> kinds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDeclaredObservability.copyWith(...)` or call `instanceOfDeclaredObservability.copyWith.fieldName(value)` for a single field.
class _$DeclaredObservabilityCWProxyImpl
    implements _$DeclaredObservabilityCWProxy {
  const _$DeclaredObservabilityCWProxyImpl(this._value);

  final DeclaredObservability _value;

  @override
  DeclaredObservability kinds(
    Map<String, DeclaredObservabilityKindsValue> kinds,
  ) => call(kinds: kinds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DeclaredObservability(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DeclaredObservability(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DeclaredObservability call({Object? kinds = const $CopyWithPlaceholder()}) {
    return DeclaredObservability(
      kinds: kinds == const $CopyWithPlaceholder() || kinds == null
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as Map<String, DeclaredObservabilityKindsValue>,
    );
  }
}

extension $DeclaredObservabilityCopyWith on DeclaredObservability {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDeclaredObservability.copyWith(...)` or `instanceOfDeclaredObservability.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DeclaredObservabilityCWProxy get copyWith =>
      _$DeclaredObservabilityCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DeclaredObservability _$DeclaredObservabilityFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DeclaredObservability', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['kinds']);
  final val = DeclaredObservability(
    kinds: $checkedConvert(
      'kinds',
      (v) => (v as Map<String, dynamic>).map(
        (k, e) => MapEntry(
          k,
          DeclaredObservabilityKindsValue.fromJson(e as Map<String, dynamic>),
        ),
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$DeclaredObservabilityToJson(
  DeclaredObservability instance,
) => <String, dynamic>{
  'kinds': instance.kinds.map((k, e) => MapEntry(k, e.toJson())),
};
