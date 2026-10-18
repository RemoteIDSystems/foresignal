// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'proposed_patch_sanity_checks.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProposedPatchSanityChecksCWProxy {
  ProposedPatchSanityChecks bracesBalanced(bool? bracesBalanced);

  ProposedPatchSanityChecks classNameUnchanged(bool? classNameUnchanged);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProposedPatchSanityChecks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProposedPatchSanityChecks(...).copyWith(id: 12, name: "My name")
  /// ```
  ProposedPatchSanityChecks call({
    bool? bracesBalanced,
    bool? classNameUnchanged,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProposedPatchSanityChecks.copyWith(...)` or call `instanceOfProposedPatchSanityChecks.copyWith.fieldName(value)` for a single field.
class _$ProposedPatchSanityChecksCWProxyImpl
    implements _$ProposedPatchSanityChecksCWProxy {
  const _$ProposedPatchSanityChecksCWProxyImpl(this._value);

  final ProposedPatchSanityChecks _value;

  @override
  ProposedPatchSanityChecks bracesBalanced(bool? bracesBalanced) =>
      call(bracesBalanced: bracesBalanced);

  @override
  ProposedPatchSanityChecks classNameUnchanged(bool? classNameUnchanged) =>
      call(classNameUnchanged: classNameUnchanged);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProposedPatchSanityChecks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProposedPatchSanityChecks(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProposedPatchSanityChecks call({
    Object? bracesBalanced = const $CopyWithPlaceholder(),
    Object? classNameUnchanged = const $CopyWithPlaceholder(),
  }) {
    return ProposedPatchSanityChecks(
      bracesBalanced: bracesBalanced == const $CopyWithPlaceholder()
          ? _value.bracesBalanced
          // ignore: cast_nullable_to_non_nullable
          : bracesBalanced as bool?,
      classNameUnchanged: classNameUnchanged == const $CopyWithPlaceholder()
          ? _value.classNameUnchanged
          // ignore: cast_nullable_to_non_nullable
          : classNameUnchanged as bool?,
    );
  }
}

extension $ProposedPatchSanityChecksCopyWith on ProposedPatchSanityChecks {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProposedPatchSanityChecks.copyWith(...)` or `instanceOfProposedPatchSanityChecks.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProposedPatchSanityChecksCWProxy get copyWith =>
      _$ProposedPatchSanityChecksCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProposedPatchSanityChecks _$ProposedPatchSanityChecksFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ProposedPatchSanityChecks', json, ($checkedConvert) {
  final val = ProposedPatchSanityChecks(
    bracesBalanced: $checkedConvert('bracesBalanced', (v) => v as bool?),
    classNameUnchanged: $checkedConvert(
      'classNameUnchanged',
      (v) => v as bool?,
    ),
  );
  return val;
});

Map<String, dynamic> _$ProposedPatchSanityChecksToJson(
  ProposedPatchSanityChecks instance,
) => <String, dynamic>{
  'bracesBalanced': ?instance.bracesBalanced,
  'classNameUnchanged': ?instance.classNameUnchanged,
};
