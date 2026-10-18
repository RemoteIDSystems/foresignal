// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'proposed_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProposedPatchCWProxy {
  ProposedPatch file(String file);

  ProposedPatch unifiedDiff(String unifiedDiff);

  ProposedPatch patchApplies(bool patchApplies);

  ProposedPatch sanityChecks(ProposedPatchSanityChecks? sanityChecks);

  ProposedPatch label(ProposedPatchLabelEnum label);

  ProposedPatch error(String? error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProposedPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProposedPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  ProposedPatch call({
    String file,
    String unifiedDiff,
    bool patchApplies,
    ProposedPatchSanityChecks? sanityChecks,
    ProposedPatchLabelEnum label,
    String? error,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProposedPatch.copyWith(...)` or call `instanceOfProposedPatch.copyWith.fieldName(value)` for a single field.
class _$ProposedPatchCWProxyImpl implements _$ProposedPatchCWProxy {
  const _$ProposedPatchCWProxyImpl(this._value);

  final ProposedPatch _value;

  @override
  ProposedPatch file(String file) => call(file: file);

  @override
  ProposedPatch unifiedDiff(String unifiedDiff) =>
      call(unifiedDiff: unifiedDiff);

  @override
  ProposedPatch patchApplies(bool patchApplies) =>
      call(patchApplies: patchApplies);

  @override
  ProposedPatch sanityChecks(ProposedPatchSanityChecks? sanityChecks) =>
      call(sanityChecks: sanityChecks);

  @override
  ProposedPatch label(ProposedPatchLabelEnum label) => call(label: label);

  @override
  ProposedPatch error(String? error) => call(error: error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProposedPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProposedPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProposedPatch call({
    Object? file = const $CopyWithPlaceholder(),
    Object? unifiedDiff = const $CopyWithPlaceholder(),
    Object? patchApplies = const $CopyWithPlaceholder(),
    Object? sanityChecks = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
  }) {
    return ProposedPatch(
      file: file == const $CopyWithPlaceholder() || file == null
          ? _value.file
          // ignore: cast_nullable_to_non_nullable
          : file as String,
      unifiedDiff:
          unifiedDiff == const $CopyWithPlaceholder() || unifiedDiff == null
          ? _value.unifiedDiff
          // ignore: cast_nullable_to_non_nullable
          : unifiedDiff as String,
      patchApplies:
          patchApplies == const $CopyWithPlaceholder() || patchApplies == null
          ? _value.patchApplies
          // ignore: cast_nullable_to_non_nullable
          : patchApplies as bool,
      sanityChecks: sanityChecks == const $CopyWithPlaceholder()
          ? _value.sanityChecks
          // ignore: cast_nullable_to_non_nullable
          : sanityChecks as ProposedPatchSanityChecks?,
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as ProposedPatchLabelEnum,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
    );
  }
}

extension $ProposedPatchCopyWith on ProposedPatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProposedPatch.copyWith(...)` or `instanceOfProposedPatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProposedPatchCWProxy get copyWith => _$ProposedPatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProposedPatch _$ProposedPatchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ProposedPatch', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['file', 'unifiedDiff', 'patchApplies', 'label'],
      );
      final val = ProposedPatch(
        file: $checkedConvert('file', (v) => v as String),
        unifiedDiff: $checkedConvert('unifiedDiff', (v) => v as String),
        patchApplies: $checkedConvert('patchApplies', (v) => v as bool),
        sanityChecks: $checkedConvert(
          'sanityChecks',
          (v) => v == null
              ? null
              : ProposedPatchSanityChecks.fromJson(v as Map<String, dynamic>),
        ),
        label: $checkedConvert(
          'label',
          (v) => $enumDecode(_$ProposedPatchLabelEnumEnumMap, v),
        ),
        error: $checkedConvert('error', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ProposedPatchToJson(ProposedPatch instance) =>
    <String, dynamic>{
      'file': instance.file,
      'unifiedDiff': instance.unifiedDiff,
      'patchApplies': instance.patchApplies,
      'sanityChecks': ?instance.sanityChecks?.toJson(),
      'label': _$ProposedPatchLabelEnumEnumMap[instance.label]!,
      'error': ?instance.error,
    };

const _$ProposedPatchLabelEnumEnumMap = {
  ProposedPatchLabelEnum.proposedCommaNotApplied: 'proposed, not applied',
};
