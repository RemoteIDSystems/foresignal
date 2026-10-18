// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_event_alternative.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelEventAlternativeCWProxy {
  IntelEventAlternative hypothesis(String hypothesis);

  IntelEventAlternative basis(String basis);

  IntelEventAlternative supportingMemberIds(List<String>? supportingMemberIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventAlternative(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventAlternative(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelEventAlternative call({
    String hypothesis,
    String basis,
    List<String>? supportingMemberIds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelEventAlternative.copyWith(...)` or call `instanceOfIntelEventAlternative.copyWith.fieldName(value)` for a single field.
class _$IntelEventAlternativeCWProxyImpl
    implements _$IntelEventAlternativeCWProxy {
  const _$IntelEventAlternativeCWProxyImpl(this._value);

  final IntelEventAlternative _value;

  @override
  IntelEventAlternative hypothesis(String hypothesis) =>
      call(hypothesis: hypothesis);

  @override
  IntelEventAlternative basis(String basis) => call(basis: basis);

  @override
  IntelEventAlternative supportingMemberIds(
    List<String>? supportingMemberIds,
  ) => call(supportingMemberIds: supportingMemberIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventAlternative(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventAlternative(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelEventAlternative call({
    Object? hypothesis = const $CopyWithPlaceholder(),
    Object? basis = const $CopyWithPlaceholder(),
    Object? supportingMemberIds = const $CopyWithPlaceholder(),
  }) {
    return IntelEventAlternative(
      hypothesis:
          hypothesis == const $CopyWithPlaceholder() || hypothesis == null
          ? _value.hypothesis
          // ignore: cast_nullable_to_non_nullable
          : hypothesis as String,
      basis: basis == const $CopyWithPlaceholder() || basis == null
          ? _value.basis
          // ignore: cast_nullable_to_non_nullable
          : basis as String,
      supportingMemberIds: supportingMemberIds == const $CopyWithPlaceholder()
          ? _value.supportingMemberIds
          // ignore: cast_nullable_to_non_nullable
          : supportingMemberIds as List<String>?,
    );
  }
}

extension $IntelEventAlternativeCopyWith on IntelEventAlternative {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelEventAlternative.copyWith(...)` or `instanceOfIntelEventAlternative.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelEventAlternativeCWProxy get copyWith =>
      _$IntelEventAlternativeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelEventAlternative _$IntelEventAlternativeFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelEventAlternative', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['hypothesis', 'basis']);
  final val = IntelEventAlternative(
    hypothesis: $checkedConvert('hypothesis', (v) => v as String),
    basis: $checkedConvert('basis', (v) => v as String),
    supportingMemberIds: $checkedConvert(
      'supportingMemberIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$IntelEventAlternativeToJson(
  IntelEventAlternative instance,
) => <String, dynamic>{
  'hypothesis': instance.hypothesis,
  'basis': instance.basis,
  'supportingMemberIds': ?instance.supportingMemberIds,
};
