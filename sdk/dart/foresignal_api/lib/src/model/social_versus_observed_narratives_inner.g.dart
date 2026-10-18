// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_versus_observed_narratives_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialVersusObservedNarrativesInnerCWProxy {
  SocialVersusObservedNarrativesInner narrativeId(String narrativeId);

  SocialVersusObservedNarrativesInner title(String title);

  SocialVersusObservedNarrativesInner status(
    SocialVersusObservedNarrativesInnerStatusEnum status,
  );

  SocialVersusObservedNarrativesInner situationId(String? situationId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialVersusObservedNarrativesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialVersusObservedNarrativesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialVersusObservedNarrativesInner call({
    String narrativeId,
    String title,
    SocialVersusObservedNarrativesInnerStatusEnum status,
    String? situationId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialVersusObservedNarrativesInner.copyWith(...)` or call `instanceOfSocialVersusObservedNarrativesInner.copyWith.fieldName(value)` for a single field.
class _$SocialVersusObservedNarrativesInnerCWProxyImpl
    implements _$SocialVersusObservedNarrativesInnerCWProxy {
  const _$SocialVersusObservedNarrativesInnerCWProxyImpl(this._value);

  final SocialVersusObservedNarrativesInner _value;

  @override
  SocialVersusObservedNarrativesInner narrativeId(String narrativeId) =>
      call(narrativeId: narrativeId);

  @override
  SocialVersusObservedNarrativesInner title(String title) => call(title: title);

  @override
  SocialVersusObservedNarrativesInner status(
    SocialVersusObservedNarrativesInnerStatusEnum status,
  ) => call(status: status);

  @override
  SocialVersusObservedNarrativesInner situationId(String? situationId) =>
      call(situationId: situationId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialVersusObservedNarrativesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialVersusObservedNarrativesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialVersusObservedNarrativesInner call({
    Object? narrativeId = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? situationId = const $CopyWithPlaceholder(),
  }) {
    return SocialVersusObservedNarrativesInner(
      narrativeId:
          narrativeId == const $CopyWithPlaceholder() || narrativeId == null
          ? _value.narrativeId
          // ignore: cast_nullable_to_non_nullable
          : narrativeId as String,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SocialVersusObservedNarrativesInnerStatusEnum,
      situationId: situationId == const $CopyWithPlaceholder()
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String?,
    );
  }
}

extension $SocialVersusObservedNarrativesInnerCopyWith
    on SocialVersusObservedNarrativesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialVersusObservedNarrativesInner.copyWith(...)` or `instanceOfSocialVersusObservedNarrativesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialVersusObservedNarrativesInnerCWProxy get copyWith =>
      _$SocialVersusObservedNarrativesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialVersusObservedNarrativesInner
_$SocialVersusObservedNarrativesInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialVersusObservedNarrativesInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['narrativeId', 'title', 'status']);
      final val = SocialVersusObservedNarrativesInner(
        narrativeId: $checkedConvert('narrativeId', (v) => v as String),
        title: $checkedConvert('title', (v) => v as String),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(
            _$SocialVersusObservedNarrativesInnerStatusEnumEnumMap,
            v,
          ),
        ),
        situationId: $checkedConvert('situationId', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SocialVersusObservedNarrativesInnerToJson(
  SocialVersusObservedNarrativesInner instance,
) => <String, dynamic>{
  'narrativeId': instance.narrativeId,
  'title': instance.title,
  'status':
      _$SocialVersusObservedNarrativesInnerStatusEnumEnumMap[instance.status]!,
  'situationId': ?instance.situationId,
};

const _$SocialVersusObservedNarrativesInnerStatusEnumEnumMap = {
  SocialVersusObservedNarrativesInnerStatusEnum.unassessed: 'unassessed',
  SocialVersusObservedNarrativesInnerStatusEnum.pending: 'pending',
  SocialVersusObservedNarrativesInnerStatusEnum.corroborated: 'corroborated',
  SocialVersusObservedNarrativesInnerStatusEnum.unobserved: 'unobserved',
  SocialVersusObservedNarrativesInnerStatusEnum.contradicted: 'contradicted',
  SocialVersusObservedNarrativesInnerStatusEnum.mixed: 'mixed',
  SocialVersusObservedNarrativesInnerStatusEnum.insufficientCoverage:
      'insufficient-coverage',
};
