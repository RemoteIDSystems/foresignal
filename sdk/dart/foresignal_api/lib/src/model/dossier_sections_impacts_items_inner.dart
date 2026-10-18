//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/impact_rate_interval.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'dossier_sections_impacts_items_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DossierSectionsImpactsItemsInner {
  /// Returns a new [DossierSectionsImpactsItemsInner] instance.
  DossierSectionsImpactsItemsInner({

    required  this.findingId,

    required  this.impactFamily,

     this.admin1,

    required  this.pImpact,

     this.interval,

     this.dependencyPaths,

    required  this.labels,

     this.questionId,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'findingId',
    required: true,
    includeIfNull: false,
  )


  final String findingId;



  @JsonKey(
    
    name: r'impactFamily',
    required: true,
    includeIfNull: false,
  )


  final String impactFamily;



  @JsonKey(
    
    name: r'admin1',
    required: false,
    includeIfNull: false,
  )


  final String? admin1;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'pImpact',
    required: true,
    includeIfNull: false,
  )


  final num pImpact;



  @JsonKey(
    
    name: r'interval',
    required: false,
    includeIfNull: false,
  )


  final ImpactRateInterval? interval;



  @JsonKey(
    
    name: r'dependencyPaths',
    required: false,
    includeIfNull: false,
  )


  final int? dependencyPaths;



  @JsonKey(
    
    name: r'labels',
    required: true,
    includeIfNull: false,
  )


  final List<String> labels;



      /// ULID identifier
  @JsonKey(
    
    name: r'questionId',
    required: false,
    includeIfNull: false,
  )


  final String? questionId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DossierSectionsImpactsItemsInner &&
      other.findingId == findingId &&
      other.impactFamily == impactFamily &&
      other.admin1 == admin1 &&
      other.pImpact == pImpact &&
      other.interval == interval &&
      other.dependencyPaths == dependencyPaths &&
      other.labels == labels &&
      other.questionId == questionId;

    @override
    int get hashCode =>
        findingId.hashCode +
        impactFamily.hashCode +
        admin1.hashCode +
        pImpact.hashCode +
        (interval == null ? 0 : interval.hashCode) +
        dependencyPaths.hashCode +
        labels.hashCode +
        (questionId == null ? 0 : questionId.hashCode);

  factory DossierSectionsImpactsItemsInner.fromJson(Map<String, dynamic> json) => _$DossierSectionsImpactsItemsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$DossierSectionsImpactsItemsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

