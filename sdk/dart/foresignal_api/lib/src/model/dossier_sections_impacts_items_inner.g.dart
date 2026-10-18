// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dossier_sections_impacts_items_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DossierSectionsImpactsItemsInnerCWProxy {
  DossierSectionsImpactsItemsInner findingId(String findingId);

  DossierSectionsImpactsItemsInner impactFamily(String impactFamily);

  DossierSectionsImpactsItemsInner admin1(String? admin1);

  DossierSectionsImpactsItemsInner pImpact(num pImpact);

  DossierSectionsImpactsItemsInner interval(ImpactRateInterval? interval);

  DossierSectionsImpactsItemsInner dependencyPaths(int? dependencyPaths);

  DossierSectionsImpactsItemsInner labels(List<String> labels);

  DossierSectionsImpactsItemsInner questionId(String? questionId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsImpactsItemsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsImpactsItemsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  DossierSectionsImpactsItemsInner call({
    String findingId,
    String impactFamily,
    String? admin1,
    num pImpact,
    ImpactRateInterval? interval,
    int? dependencyPaths,
    List<String> labels,
    String? questionId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDossierSectionsImpactsItemsInner.copyWith(...)` or call `instanceOfDossierSectionsImpactsItemsInner.copyWith.fieldName(value)` for a single field.
class _$DossierSectionsImpactsItemsInnerCWProxyImpl
    implements _$DossierSectionsImpactsItemsInnerCWProxy {
  const _$DossierSectionsImpactsItemsInnerCWProxyImpl(this._value);

  final DossierSectionsImpactsItemsInner _value;

  @override
  DossierSectionsImpactsItemsInner findingId(String findingId) =>
      call(findingId: findingId);

  @override
  DossierSectionsImpactsItemsInner impactFamily(String impactFamily) =>
      call(impactFamily: impactFamily);

  @override
  DossierSectionsImpactsItemsInner admin1(String? admin1) =>
      call(admin1: admin1);

  @override
  DossierSectionsImpactsItemsInner pImpact(num pImpact) =>
      call(pImpact: pImpact);

  @override
  DossierSectionsImpactsItemsInner interval(ImpactRateInterval? interval) =>
      call(interval: interval);

  @override
  DossierSectionsImpactsItemsInner dependencyPaths(int? dependencyPaths) =>
      call(dependencyPaths: dependencyPaths);

  @override
  DossierSectionsImpactsItemsInner labels(List<String> labels) =>
      call(labels: labels);

  @override
  DossierSectionsImpactsItemsInner questionId(String? questionId) =>
      call(questionId: questionId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsImpactsItemsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsImpactsItemsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DossierSectionsImpactsItemsInner call({
    Object? findingId = const $CopyWithPlaceholder(),
    Object? impactFamily = const $CopyWithPlaceholder(),
    Object? admin1 = const $CopyWithPlaceholder(),
    Object? pImpact = const $CopyWithPlaceholder(),
    Object? interval = const $CopyWithPlaceholder(),
    Object? dependencyPaths = const $CopyWithPlaceholder(),
    Object? labels = const $CopyWithPlaceholder(),
    Object? questionId = const $CopyWithPlaceholder(),
  }) {
    return DossierSectionsImpactsItemsInner(
      findingId: findingId == const $CopyWithPlaceholder() || findingId == null
          ? _value.findingId
          // ignore: cast_nullable_to_non_nullable
          : findingId as String,
      impactFamily:
          impactFamily == const $CopyWithPlaceholder() || impactFamily == null
          ? _value.impactFamily
          // ignore: cast_nullable_to_non_nullable
          : impactFamily as String,
      admin1: admin1 == const $CopyWithPlaceholder()
          ? _value.admin1
          // ignore: cast_nullable_to_non_nullable
          : admin1 as String?,
      pImpact: pImpact == const $CopyWithPlaceholder() || pImpact == null
          ? _value.pImpact
          // ignore: cast_nullable_to_non_nullable
          : pImpact as num,
      interval: interval == const $CopyWithPlaceholder()
          ? _value.interval
          // ignore: cast_nullable_to_non_nullable
          : interval as ImpactRateInterval?,
      dependencyPaths: dependencyPaths == const $CopyWithPlaceholder()
          ? _value.dependencyPaths
          // ignore: cast_nullable_to_non_nullable
          : dependencyPaths as int?,
      labels: labels == const $CopyWithPlaceholder() || labels == null
          ? _value.labels
          // ignore: cast_nullable_to_non_nullable
          : labels as List<String>,
      questionId: questionId == const $CopyWithPlaceholder()
          ? _value.questionId
          // ignore: cast_nullable_to_non_nullable
          : questionId as String?,
    );
  }
}

extension $DossierSectionsImpactsItemsInnerCopyWith
    on DossierSectionsImpactsItemsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDossierSectionsImpactsItemsInner.copyWith(...)` or `instanceOfDossierSectionsImpactsItemsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DossierSectionsImpactsItemsInnerCWProxy get copyWith =>
      _$DossierSectionsImpactsItemsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DossierSectionsImpactsItemsInner _$DossierSectionsImpactsItemsInnerFromJson(
  Map<String, dynamic> json,
) =>
    $checkedCreate('DossierSectionsImpactsItemsInner', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['findingId', 'impactFamily', 'pImpact', 'labels'],
      );
      final val = DossierSectionsImpactsItemsInner(
        findingId: $checkedConvert('findingId', (v) => v as String),
        impactFamily: $checkedConvert('impactFamily', (v) => v as String),
        admin1: $checkedConvert('admin1', (v) => v as String?),
        pImpact: $checkedConvert('pImpact', (v) => v as num),
        interval: $checkedConvert(
          'interval',
          (v) => v == null
              ? null
              : ImpactRateInterval.fromJson(v as Map<String, dynamic>),
        ),
        dependencyPaths: $checkedConvert(
          'dependencyPaths',
          (v) => (v as num?)?.toInt(),
        ),
        labels: $checkedConvert(
          'labels',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        questionId: $checkedConvert('questionId', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$DossierSectionsImpactsItemsInnerToJson(
  DossierSectionsImpactsItemsInner instance,
) => <String, dynamic>{
  'findingId': instance.findingId,
  'impactFamily': instance.impactFamily,
  'admin1': ?instance.admin1,
  'pImpact': instance.pImpact,
  'interval': ?instance.interval?.toJson(),
  'dependencyPaths': ?instance.dependencyPaths,
  'labels': instance.labels,
  'questionId': ?instance.questionId,
};
