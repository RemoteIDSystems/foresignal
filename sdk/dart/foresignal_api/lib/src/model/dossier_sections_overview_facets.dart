//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'dossier_sections_overview_facets.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DossierSectionsOverviewFacets {
  /// Returns a new [DossierSectionsOverviewFacets] instance.
  DossierSectionsOverviewFacets({

     this.byEvidenceClass,

     this.bySource,
  });

      /// imagery | signals | reports | documents | sensor | social | other
  @JsonKey(
    
    name: r'byEvidenceClass',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? byEvidenceClass;



  @JsonKey(
    
    name: r'bySource',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? bySource;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DossierSectionsOverviewFacets &&
      other.byEvidenceClass == byEvidenceClass &&
      other.bySource == bySource;

    @override
    int get hashCode =>
        byEvidenceClass.hashCode +
        bySource.hashCode;

  factory DossierSectionsOverviewFacets.fromJson(Map<String, dynamic> json) => _$DossierSectionsOverviewFacetsFromJson(json);

  Map<String, dynamic> toJson() => _$DossierSectionsOverviewFacetsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

